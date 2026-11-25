package com.isil.edu.pe.pedidos.reportes;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class PedidoRevisionApiIntegrationTest {

  private MockMvc mockMvc;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private WebApplicationContext context;

  @BeforeEach
  void prepararPedidos() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    jdbcTemplate.update("DELETE FROM pedido_revision");
    jdbcTemplate.update("DELETE FROM pedido");
    insertarPedido(101, "Ana Torres", false);
    insertarPedido(102, "Carlos Perez", true);
    jdbcTemplate.update(
        "INSERT INTO pedido_revision (pedido_id, observacion, revisado_por) VALUES (?, ?, ?)",
        102,
        "Validación de los datos del pedido.",
        "operador"
    );
  }

  @Test
  void listsRevisionStateAsJsonWithAnExplicitStateFilter() throws Exception {
    mockMvc.perform(get("/api/v1/pedidos/revisiones").param("estado", "PENDIENTE"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].pedidoId").value(101))
        .andExpect(jsonPath("$[0].estado").value("PENDIENTE"))
        .andExpect(jsonPath("$[0].revisadoPor").doesNotExist());

    mockMvc.perform(get("/api/v1/pedidos/revisiones").param("estado", "REVISADO"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].pedidoId").value(102))
        .andExpect(jsonPath("$[0].estado").value("REVISADO"))
        .andExpect(jsonPath("$[0].resultado").value("RECHAZADO"))
        .andExpect(jsonPath("$[0].observacion").value("Validación de los datos del pedido."))
        .andExpect(jsonPath("$[0].revisadoPor").value("operador"))
        .andExpect(jsonPath("$[0].fechaRevision").exists());
  }

  @Test
  void returnsReviewStatisticsAndBadRequestForInvalidState() throws Exception {
    mockMvc.perform(get("/api/v1/pedidos/estadisticas/revisiones"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalPedidos").value(2))
        .andExpect(jsonPath("$.totalPendientes").value(1))
        .andExpect(jsonPath("$.totalRevisados").value(1))
        .andExpect(jsonPath("$.porcentajeRevisado").value(50.0));

    mockMvc.perform(get("/api/v1/pedidos/revisiones").param("estado", "OTRO"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void returnsZeroReviewPercentageWhenThereAreNoOrders() throws Exception {
    jdbcTemplate.update("DELETE FROM pedido_revision");
    jdbcTemplate.update("DELETE FROM pedido");

    mockMvc.perform(get("/api/v1/pedidos/estadisticas/revisiones"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalPedidos").value(0))
        .andExpect(jsonPath("$.porcentajeRevisado").value(0.0));
  }

  private void insertarPedido(long id, String cliente, boolean revisado) {
    jdbcTemplate.update(
        """
        INSERT INTO pedido (
          id, cliente, producto_codigo, producto_nombre, precio_unitario,
          cantidad, total, fecha, revisado, estado_revision
        ) VALUES (?, ?, 'LAP-001', 'Laptop', ?, 1, ?, CURRENT_TIMESTAMP, ?, ?)
        """,
        id,
        cliente,
        new BigDecimal("100.00"),
        new BigDecimal("100.00"),
        revisado,
        revisado ? "RECHAZADO" : "PENDIENTE"
    );
  }
}
