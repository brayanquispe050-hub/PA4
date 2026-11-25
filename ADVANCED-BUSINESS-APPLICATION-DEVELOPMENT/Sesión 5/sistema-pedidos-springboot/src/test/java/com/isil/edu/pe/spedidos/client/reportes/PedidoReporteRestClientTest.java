package com.isil.edu.pe.spedidos.client.reportes;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class PedidoReporteRestClientTest {

  @Test
  void mapsRemoteServerErrorToUnavailableException() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    PedidoReporteRestClient client = new PedidoReporteRestClient(
        builder.build(),
        "http://reportes.test/api/v1/pedidos"
    );
    server.expect(requestTo("http://reportes.test/api/v1/pedidos/estadisticas/revisiones"))
        .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

    assertThrows(PedidoReporteNoDisponibleException.class, client::obtenerEstadisticasRevisiones);
    server.verify();
  }

  @Test
  void mapsConnectionFailureToUnavailableException() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    PedidoReporteRestClient client = new PedidoReporteRestClient(
        builder.build(),
        "http://reportes.test/api/v1/pedidos"
    );
    server.expect(requestTo("http://reportes.test/api/v1/pedidos/revisiones?estado=PENDIENTE"))
        .andRespond(withException(new IOException("connection refused")));

    assertThrows(
        PedidoReporteNoDisponibleException.class,
        () -> client.listarRevisiones("PENDIENTE")
    );
    server.verify();
  }
}
