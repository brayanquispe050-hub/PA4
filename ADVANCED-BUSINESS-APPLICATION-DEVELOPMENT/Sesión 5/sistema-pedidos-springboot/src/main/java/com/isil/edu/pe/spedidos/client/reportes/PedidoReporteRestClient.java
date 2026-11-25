package com.isil.edu.pe.spedidos.client.reportes;

import com.isil.edu.pe.spedidos.client.reportes.dto.PedidoReporteResponse;
import com.isil.edu.pe.spedidos.client.reportes.dto.PedidoRevisionEstadisticasResponse;
import com.isil.edu.pe.spedidos.client.reportes.dto.PedidoRevisionResponse;
import com.isil.edu.pe.spedidos.client.reportes.dto.ProductoVendidoResponse;
import com.isil.edu.pe.spedidos.client.reportes.dto.ResumenPedidosResponse;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class PedidoReporteRestClient implements PedidoReporteClient {

  private final RestClient restClient;
  private final String baseUrl;

  public PedidoReporteRestClient(
      @Qualifier("pedidoReporteHttpClient") RestClient restClient,
      @Value("${pedido-reportes.api.base-url}") String baseUrl) {
    this.restClient = restClient;
    this.baseUrl = baseUrl;
  }

  @Override
  public ResumenPedidosResponse obtenerResumen() {
    ResumenPedidosResponse response = ejecutar(() -> restClient.get()
        .uri(baseUrl + "/estadisticas/resumen")
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(ResumenPedidosResponse.class));
    if (response == null) {
      throw new PedidoReporteNoDisponibleException("El servicio de reportes devolvió una respuesta vacía", null);
    }
    return response;
  }

  @Override
  public List<PedidoReporteResponse> buscarPorCliente(String cliente) {
    if (cliente == null || cliente.isBlank()) {
      return List.of();
    }
    List<PedidoReporteResponse> response = ejecutar(() -> restClient.get()
          .uri(baseUrl + "?cliente={cliente}", cliente.trim())
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .body(new ParameterizedTypeReference<List<PedidoReporteResponse>>() { }));
    return response == null ? List.of() : response;
  }

  @Override
  public List<ProductoVendidoResponse> listarProductosMasVendidos() {
    List<ProductoVendidoResponse> response = ejecutar(() -> restClient.get()
          .uri(baseUrl + "/estadisticas/productos")
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .body(new ParameterizedTypeReference<List<ProductoVendidoResponse>>() { }));
    return response == null ? List.of() : response;
  }

  @Override
  public List<PedidoRevisionResponse> listarRevisiones(String estado) {
    if (!"PENDIENTE".equals(estado) && !"REVISADO".equals(estado)) {
      throw new IllegalArgumentException("El estado debe ser PENDIENTE o REVISADO.");
    }
    List<PedidoRevisionResponse> response = ejecutar(() -> restClient.get()
        .uri(baseUrl + "/revisiones?estado={estado}", estado)
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(new ParameterizedTypeReference<List<PedidoRevisionResponse>>() { }));
    return response == null ? List.of() : response;
  }

  @Override
  public PedidoRevisionEstadisticasResponse obtenerEstadisticasRevisiones() {
    PedidoRevisionEstadisticasResponse response = ejecutar(() -> restClient.get()
        .uri(baseUrl + "/estadisticas/revisiones")
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(PedidoRevisionEstadisticasResponse.class));
    if (response == null) {
      throw new PedidoReporteNoDisponibleException("El servicio de reportes devolvió una respuesta vacía", null);
    }
    return response;
  }

  private <T> T ejecutar(Supplier<T> solicitud) {
    try {
      return solicitud.get();
    } catch (ResourceAccessException e) {
      throw noDisponible(e);
    } catch (RestClientResponseException e) {
      if (e.getStatusCode().is5xxServerError()) {
        throw noDisponible(e);
      }
      throw e;
    }
  }

  private PedidoReporteNoDisponibleException noDisponible(Exception e) {
    return new PedidoReporteNoDisponibleException(
        "pedidos-reportes-service no está disponible. Verifique que se ejecute en el puerto 8083.", e);
  }
}
