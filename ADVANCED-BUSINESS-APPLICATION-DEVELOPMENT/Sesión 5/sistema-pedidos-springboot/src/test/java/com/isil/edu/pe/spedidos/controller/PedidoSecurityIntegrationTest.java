package com.isil.edu.pe.spedidos.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

import com.isil.edu.pe.spedidos.service.PedidoService;
import com.isil.edu.pe.spedidos.client.inventario.dto.ProductoInventarioResponse;
import com.isil.edu.pe.spedidos.entity.Pedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
class PedidoSecurityIntegrationTest {

  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext context;

  @MockitoBean
  private PedidoService pedidoService;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
        .apply(springSecurity())
        .build();
  }

  @Test
  @DisplayName("Redirect anonymous user to login when accessing orders page")
  void redirectAnonymousUserToLoginWhenAccessingOrdersPage() throws Exception {
    // Arrange / Act
    ResultActions response = mockMvc.perform(get("/pedidos"));
    // Assert: no se llama inventario porque seguridad intercepta primero.
    response.andExpect(status().is3xxRedirection());
  }

  @Test
  void showSupervisionNavigationOnlyToOperators() throws Exception {
    given(pedidoService.listarProductos()).willReturn(List.of());
    given(pedidoService.listarPedidos()).willReturn(List.of());

    mockMvc.perform(get("/pedidos")
            .with(user("operador").roles("OPERADOR")))
        .andExpect(status().isOk())
        .andExpect(view().name("pedidos"))
        .andExpect(model().attribute("puedeSupervisar", true));

    mockMvc.perform(get("/pedidos")
            .with(user("consulta").roles("CONSULTA")))
        .andExpect(status().isOk())
        .andExpect(model().attribute("puedeSupervisar", false));

  }

  @Test
  void operatorCanOpenProductSupervisionAndConsultRoleCannot() throws Exception {
    ProductoInventarioResponse producto = new ProductoInventarioResponse();
    producto.setCodigo("LAP-001");
    producto.setNombre("Laptop");
    producto.setPrecio(new BigDecimal("2500.00"));
    producto.setStock(3);
    given(pedidoService.listarProductos()).willReturn(List.of(producto));

    mockMvc.perform(get("/supervision/productos")
            .with(user("operador").roles("OPERADOR")))
        .andExpect(status().isOk())
        .andExpect(view().name("supervision-productos"))
        .andExpect(model().attribute("productosDisponibles", 1L));

    mockMvc.perform(get("/supervision/productos")
            .with(user("consulta").roles("CONSULTA")))
        .andExpect(status().isForbidden());
  }

  @Test
  void orderListShowsPendingAcceptedRejectedAndProductSupervisionLink() throws Exception {
    Pedido pendiente = pedido(1L, false);
    Pedido aceptado = pedido(2L, true);
    aceptado.setEstadoRevision("ACEPTADO");
    Pedido rechazado = pedido(3L, true);
    rechazado.setEstadoRevision("RECHAZADO");
    assertEquals("PENDIENTE", pendiente.getEstadoRevision());
    assertEquals("ACEPTADO", aceptado.getEstadoRevision());
    assertEquals("RECHAZADO", rechazado.getEstadoRevision());
    given(pedidoService.listarProductos()).willReturn(List.of());
    given(pedidoService.listarPedidos()).willReturn(List.of(pendiente, aceptado, rechazado));

    mockMvc.perform(get("/pedidos").with(user("operador").roles("OPERADOR")))
        .andExpect(status().isOk())
        .andExpect(view().name("pedidos"))
        .andExpect(model().attributeExists("pedidos", "puedeSupervisar"));
  }

  private Pedido pedido(Long id, boolean revisado) {
    Pedido pedido = new Pedido(
        id,
        "Ana Torres",
        "LAP-001",
        "Laptop",
        new BigDecimal("2500.00"),
        1,
        new BigDecimal("2500.00"),
        LocalDateTime.now()
    );
    pedido.setRevisado(revisado);
    return pedido;
  }

  @Test
  @DisplayName("Reject PedidoController POST when CONSULTA role submits valid CSRF")
  void rejectPedidoControllerPostWhenConsultaRoleSubmitsValidCsrf() throws Exception {
    // Arrange / Act
    ResultActions response = mockMvc.perform(post("/pedidos")
        .with(user("consulta").roles("CONSULTA"))
        .with(csrf())
        .param("cliente", "Ana")
        .param("productoCodigo", "P001")
        .param("cantidad", "1"));
    // Assert: no se llega al servicio ni a inventario.
    response.andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Reject PedidoController POST when OPERADOR role omits CSRF token")
  void rejectPedidoControllerPostWhenOperadorRoleOmitsCsrfToken() throws Exception {
    // Arrange / Act
    ResultActions response = mockMvc.perform(post("/pedidos")
        .with(user("operador").roles("OPERADOR"))
        .param("cliente", "Ana")
        .param("productoCodigo", "P001")
        .param("cantidad", "1"));
    // Assert: la validación CSRF ocurre antes del negocio.
    response.andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Return order summary JSON when CONSULTA role accesses protected API")
  void returnOrderSummaryJsonWhenConsultaRoleAccessesProtectedApi() throws Exception {
    // Arrange / Act
    ResultActions response = mockMvc.perform(get("/api/v1/orders/resumen")
        .with(user("consulta").roles("CONSULTA")));
    // Assert
    response.andExpect(status().isOk())
        .andExpect(jsonPath("$.totalPedidos").isNumber())
        .andExpect(jsonPath("$.totalUnidades").isNumber());
  }
}
