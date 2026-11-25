package com.isil.edu.pe.spedidos.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.mockito.Mockito.when;

import com.isil.edu.pe.spedidos.client.reportes.PedidoReporteClient;
import com.isil.edu.pe.spedidos.client.reportes.dto.ResumenPedidosResponse;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
class PedidoReporteWebControllerTest {

  @MockitoBean
  private PedidoReporteClient pedidoReporteClient;

  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext context;

  @BeforeEach
  void setUp() {
    when(pedidoReporteClient.listarProductosMasVendidos()).thenReturn(List.of());
    when(pedidoReporteClient.buscarPorCliente("Ana")).thenReturn(List.of());
    when(pedidoReporteClient.obtenerResumen()).thenReturn(
        new ResumenPedidosResponse(0, 0, BigDecimal.ZERO, BigDecimal.ZERO)
    );
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
        .apply(springSecurity())
        .build();
  }

  @Test
  @DisplayName("Render report view with expected model for CONSULTA user")
  void renderReportViewWithExpectedModelForConsultaUser() throws Exception {
    mockMvc.perform(get("/reportes/pedidos")
            .param("cliente", "Ana")
            .with(user("consulta").roles("CONSULTA")))
        .andExpect(status().isOk())
        .andExpect(view().name("reportes-pedidos"))
        .andExpect(model().attributeExists("resumen", "productos", "pedidos", "cliente"));
  }

}
