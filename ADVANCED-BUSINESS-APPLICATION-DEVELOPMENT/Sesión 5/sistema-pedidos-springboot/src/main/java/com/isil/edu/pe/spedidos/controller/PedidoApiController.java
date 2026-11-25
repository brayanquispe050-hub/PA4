package com.isil.edu.pe.spedidos.controller;

import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.service.PedidoService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/orders", produces = MediaType.APPLICATION_JSON_VALUE)
public class PedidoApiController {

  private final PedidoService pedidoService;

  public PedidoApiController(PedidoService pedidoService) {
    this.pedidoService = pedidoService;
  }

  @GetMapping
  public List<Pedido> listar() {
    return pedidoService.listarPedidos();
  }

  @GetMapping("/resumen")
  public ResumenPedidosResponse obtenerResumen() {
    List<Pedido> pedidos = pedidoService.listarPedidos();
    return new ResumenPedidosResponse(
        "Sistema de Pedidos",
        pedidos.size(),
        pedidos.stream().mapToInt(Pedido::getCantidad).sum()
    );
  }

  public record ResumenPedidosResponse(String modulo, int totalPedidos, int totalUnidades) {
  }
}
