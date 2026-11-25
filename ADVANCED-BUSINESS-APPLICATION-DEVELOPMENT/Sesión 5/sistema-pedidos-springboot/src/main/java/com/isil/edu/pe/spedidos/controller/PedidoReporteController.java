package com.isil.edu.pe.spedidos.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.repository.projection.ProductoVendidoProjection;
import com.isil.edu.pe.spedidos.repository.projection.ResumenPedidosProjection;
import com.isil.edu.pe.spedidos.service.PedidoReporteService;
import java.util.List;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoReporteController {

  private final PedidoReporteService reporteService;

  public PedidoReporteController(PedidoReporteService reporteService) {
    this.reporteService = reporteService;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Pedido> buscarPorCliente(@RequestParam String cliente) {
    return reporteService.buscarPorCliente(cliente);
  }

  @GetMapping(value = "/estadisticas/resumen", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ResumenPedidosProjection> obtenerResumen() {
    return ResponseEntity.ok(reporteService.obtenerResumen());
  }

  @GetMapping(value = "/estadisticas/productos", produces = MediaTypes.HAL_JSON_VALUE)
  public CollectionModel<ProductoVendidoProjection> listarProductosMasVendidos() {
    List<ProductoVendidoProjection> productos = reporteService.listarProductosMasVendidos();
    Link self = linkTo(methodOn(PedidoReporteController.class).listarProductosMasVendidos())
        .withSelfRel();
    Link resumen = linkTo(methodOn(PedidoReporteController.class).obtenerResumen())
        .withRel("estadisticas-resumen");
    Link pedidos = linkTo(methodOn(PedidoReporteController.class).buscarPorCliente(""))
        .withRel("pedidos");
    return CollectionModel.of(productos, self, resumen, pedidos);
  }
}
