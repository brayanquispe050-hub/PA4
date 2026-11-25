package com.isil.edu.pe.spedidos.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

import com.isil.edu.pe.spedidos.client.reportes.PedidoReporteClient;
import com.isil.edu.pe.spedidos.client.reportes.PedidoReporteNoDisponibleException;
import com.isil.edu.pe.spedidos.client.reportes.dto.PedidoRevisionEstadisticasResponse;
import com.isil.edu.pe.spedidos.client.reportes.dto.PedidoRevisionResponse;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
class SupervisionPedidoControllerIntegrationTest {

  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @MockitoBean
  private PedidoReporteClient pedidoReporteClient;

  private long pedidoId;
  private PedidoRevisionResponse pedidoPendiente;

  @BeforeEach
  void prepararPedidoPendiente() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
        .apply(springSecurity())
        .build();
    jdbcTemplate.update("DELETE FROM pedido_revision");
    jdbcTemplate.update("DELETE FROM pedido");
    jdbcTemplate.update(
        """
        INSERT INTO pedido (
          cliente, producto_codigo, producto_nombre, precio_unitario,
          cantidad, total, fecha, revisado
        ) VALUES ('Ana Torres', 'LAP-001', 'Laptop', 100.00, 1, 100.00, CURRENT_TIMESTAMP, FALSE)
        """
    );
    pedidoId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM pedido", Long.class);
    pedidoPendiente = new PedidoRevisionResponse(
        pedidoId,
        "Ana Torres",
        "LAP-001",
        "Laptop",
        new BigDecimal("100.00"),
        "PENDIENTE",
        null,
        null,
        null,
        null
    );
    given(pedidoReporteClient.listarRevisiones("REVISADO")).willReturn(List.of());
    given(pedidoReporteClient.obtenerEstadisticasRevisiones()).willReturn(
        new PedidoRevisionEstadisticasResponse(1, 0, 1, BigDecimal.ZERO)
    );
  }

  @Test
  void operatorCanViewQueueAndSubmitCsrfProtectedReviewWithPrg() throws Exception {
    mockMvc.perform(get("/supervision/pedidos")
            .with(user("operador").roles("OPERADOR")))
        .andExpect(status().isOk())
        .andExpect(view().name("supervision-pedidos"))
        .andExpect(model().attributeExists(
            "estadisticasRevisiones",
            "pedidosRevisados"
        ))
        .andExpect(model().attribute("pedidosPendientes", List.of(pedidoPendiente)));

    mockMvc.perform(post("/supervision/pedidos/{id}/revisar", pedidoId)
            .with(user("operador").roles("OPERADOR"))
            .with(csrf())
            .param("resultado", "ACEPTADO")
            .param("observacion", "Pedido verificado."))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attributeExists("mensaje"));

    assertEquals(Boolean.TRUE, jdbcTemplate.queryForObject(
        "SELECT revisado FROM pedido WHERE id = ?",
        Boolean.class,
        pedidoId
    ));
    assertEquals("Pedido verificado.", jdbcTemplate.queryForObject(
        "SELECT observacion FROM pedido_revision WHERE pedido_id = ?",
        String.class,
        pedidoId
    ));
    assertEquals("ACEPTADO", jdbcTemplate.queryForObject(
        "SELECT estado_revision FROM pedido WHERE id = ?",
        String.class,
        pedidoId
    ));
  }

  @Test
  void reportsRemoteServiceUnavailableWithoutLeakingTechnicalDetails() throws Exception {
    given(pedidoReporteClient.listarRevisiones("REVISADO"))
        .willThrow(new PedidoReporteNoDisponibleException("Reportes temporalmente no disponibles.", null));

    mockMvc.perform(get("/supervision/pedidos")
            .with(user("operador").roles("OPERADOR")))
        .andExpect(status().isOk())
        .andExpect(view().name("supervision-pedidos"))
        .andExpect(model().attribute("pedidosPendientes", List.of(pedidoPendiente)))
        .andExpect(model().attribute(
            "errorReporte",
            "No se pudo cargar el historial de supervisiones. Los pedidos pendientes siguen disponibles."
        ));
  }

  @Test
  void duplicateReviewIsRedirectedWithErrorAndDoesNotCreateAnotherAudit() throws Exception {
    mockMvc.perform(post("/supervision/pedidos/{id}/revisar", pedidoId)
            .with(user("operador").roles("OPERADOR"))
            .with(csrf())
            .param("resultado", "ACEPTADO")
            .param("observacion", "Primera revisión."))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attributeExists("mensaje"));

    mockMvc.perform(post("/supervision/pedidos/{id}/revisar", pedidoId)
            .with(user("operador").roles("OPERADOR"))
            .with(csrf())
            .param("resultado", "ACEPTADO")
            .param("observacion", "Reintento."))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attributeExists("error"));

    assertEquals(1, jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM pedido_revision WHERE pedido_id = ?",
        Integer.class,
        pedidoId
    ));
  }

  @Test
  void operatorCanRejectOrderAndPersistTheDecision() throws Exception {
    mockMvc.perform(post("/supervision/pedidos/{id}/revisar", pedidoId)
            .with(user("operador").roles("OPERADOR"))
            .with(csrf())
            .param("resultado", "RECHAZADO")
            .param("observacion", "Falta validar el precio."))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attributeExists("mensaje"));

    assertEquals(Boolean.TRUE, jdbcTemplate.queryForObject(
        "SELECT revisado FROM pedido WHERE id = ?",
        Boolean.class,
        pedidoId
    ));
    assertEquals("RECHAZADO", jdbcTemplate.queryForObject(
        "SELECT estado_revision FROM pedido WHERE id = ?",
        String.class,
        pedidoId
    ));
  }

  @Test
  void supervisionDashboardSeparatesAcceptedAndRejectedCounts() throws Exception {
    given(pedidoReporteClient.listarRevisiones("REVISADO")).willReturn(List.of(
        new PedidoRevisionResponse(
            20,
            "Ana Torres",
            "LAP-001",
            "Laptop",
            new BigDecimal("100.00"),
            "REVISADO",
            "ACEPTADO",
            "Correcto.",
            "operador",
            null
        ),
        new PedidoRevisionResponse(
            19,
            "Carlos Perez",
            "MOU-001",
            "Mouse",
            new BigDecimal("50.00"),
            "REVISADO",
            "RECHAZADO",
            "Falta validar.",
            "operador",
            null
        )
    ));

    mockMvc.perform(get("/supervision/pedidos")
            .with(user("operador").roles("OPERADOR")))
        .andExpect(status().isOk())
        .andExpect(model().attribute("totalAceptados", 1L))
        .andExpect(model().attribute("totalRechazados", 1L));
  }

  @Test
  void invalidIdAndBlankObservationAreRedirectedWithoutWriting() throws Exception {
    mockMvc.perform(post("/supervision/pedidos/{id}/revisar", -1)
            .with(user("operador").roles("OPERADOR"))
            .with(csrf())
            .param("resultado", "ACEPTADO")
            .param("observacion", "Inválido."))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attributeExists("error"));

    mockMvc.perform(post("/supervision/pedidos/{id}/revisar", pedidoId)
            .with(user("operador").roles("OPERADOR"))
            .with(csrf())
            .param("resultado", "ACEPTADO")
            .param("observacion", "  "))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attributeExists("error"));

    mockMvc.perform(post("/supervision/pedidos/{id}/revisar", pedidoId + 100)
            .with(user("operador").roles("OPERADOR"))
            .with(csrf())
            .param("resultado", "ACEPTADO")
            .param("observacion", "Pedido no registrado."))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attributeExists("error"));

    assertEquals(Boolean.FALSE, jdbcTemplate.queryForObject(
        "SELECT revisado FROM pedido WHERE id = ?",
        Boolean.class,
        pedidoId
    ));
    assertEquals(0, jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM pedido_revision WHERE pedido_id = ?",
        Integer.class,
        pedidoId
    ));
  }

  @Test
  void rejectsConsultRoleAndPostWithoutCsrf() throws Exception {
    mockMvc.perform(get("/supervision/pedidos")
            .with(user("consulta").roles("CONSULTA")))
        .andExpect(status().isForbidden());

    mockMvc.perform(post("/supervision/pedidos/{id}/revisar", pedidoId)
            .with(user("operador").roles("OPERADOR")))
        .andExpect(status().isForbidden());

    assertEquals(Boolean.FALSE, jdbcTemplate.queryForObject(
        "SELECT revisado FROM pedido WHERE id = ?",
        Boolean.class,
        pedidoId
    ));
  }
}
