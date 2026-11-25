package com.isil.edu.pe.spedidos.controller;

import com.isil.edu.pe.spedidos.exception.PedidoRevisionNoEncontradaException;
import com.isil.edu.pe.spedidos.exception.PedidoYaRevisadoException;
import com.isil.edu.pe.spedidos.client.reportes.PedidoReporteClient;
import com.isil.edu.pe.spedidos.client.reportes.PedidoReporteNoDisponibleException;
import com.isil.edu.pe.spedidos.client.reportes.dto.PedidoRevisionEstadisticasResponse;
import com.isil.edu.pe.spedidos.client.reportes.dto.PedidoRevisionResponse;
import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.service.PedidoRevisionService;
import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/supervision/pedidos")
public class SupervisionPedidoController {

  private final PedidoRevisionService revisionService;
  private final PedidoReporteClient pedidoReporteClient;

  public SupervisionPedidoController(
      PedidoRevisionService revisionService,
      PedidoReporteClient pedidoReporteClient) {
    this.revisionService = revisionService;
    this.pedidoReporteClient = pedidoReporteClient;
  }

  @GetMapping
  public String listarPendientes(Model model) {
    List<PedidoRevisionResponse> pendientes = revisionService.listarPendientes()
        .stream()
        .map(this::toPedidoRevisionResponse)
        .toList();
    List<PedidoRevisionResponse> revisados = List.of();
    PedidoRevisionEstadisticasResponse estadisticas = null;
    try {
      revisados = pedidoReporteClient.listarRevisiones("REVISADO")
          .stream()
          .sorted(Comparator.comparingLong(PedidoRevisionResponse::pedidoId).reversed())
          .toList();
      estadisticas = pedidoReporteClient.obtenerEstadisticasRevisiones();
    } catch (PedidoReporteNoDisponibleException e) {
      model.addAttribute(
          "errorReporte",
          "No se pudo cargar el historial de supervisiones. Los pedidos pendientes siguen disponibles."
      );
    }

    List<PedidoRevisionResponse> pedidos = Stream.concat(pendientes.stream(), revisados.stream())
        .sorted(Comparator.comparingLong(PedidoRevisionResponse::pedidoId).reversed())
        .toList();
    model.addAttribute("pedidos", pedidos);
    model.addAttribute("pedidosPendientes", pendientes);
    model.addAttribute("pedidosRevisados", revisados);
    if (estadisticas != null) {
      model.addAttribute("estadisticasRevisiones", estadisticas);
    }
    model.addAttribute(
        "totalAceptados",
        revisados.stream().filter(pedido -> "ACEPTADO".equals(pedido.resultado())).count()
    );
    model.addAttribute(
        "totalRechazados",
        revisados.stream().filter(pedido -> "RECHAZADO".equals(pedido.resultado())).count()
    );
    return "supervision-pedidos";
  }

  private PedidoRevisionResponse toPedidoRevisionResponse(Pedido pedido) {
    return new PedidoRevisionResponse(
        pedido.getId(),
        pedido.getCliente(),
        pedido.getProductoCodigo(),
        pedido.getProductoNombre(),
        pedido.getTotal(),
        "PENDIENTE",
        null,
        null,
        null,
        null
    );
  }

  @PostMapping("/{id}/revisar")
  public String revisar(
      @PathVariable long id,
      @RequestParam("resultado") String resultado,
      @RequestParam("observacion") String observacion,
      Principal principal,
      RedirectAttributes redirectAttributes) {
    try {
      revisionService.revisarPedido(id, resultado, observacion, principal.getName());
      redirectAttributes.addFlashAttribute(
          "mensaje",
          "El pedido fue marcado como " + resultado.toLowerCase() + "."
      );
    } catch (PedidoYaRevisadoException | PedidoRevisionNoEncontradaException
        | IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/supervision/pedidos";
  }
}
