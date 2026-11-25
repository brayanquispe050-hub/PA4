package com.isil.edu.pe.spedidos.controller;

import com.isil.edu.pe.spedidos.client.inventario.InventarioNoDisponibleException;
import com.isil.edu.pe.spedidos.client.inventario.dto.ProductoInventarioResponse;
import com.isil.edu.pe.spedidos.service.PedidoService;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/supervision/productos")
public class SupervisionProductoController {

  private final PedidoService pedidoService;

  public SupervisionProductoController(PedidoService pedidoService) {
    this.pedidoService = pedidoService;
  }

  @GetMapping
  public String listar(Model model, HttpServletResponse response) {
    try {
      List<ProductoInventarioResponse> productos = pedidoService.listarProductos();
      model.addAttribute("productos", productos);
      model.addAttribute(
          "productosDisponibles",
          productos.stream().filter(producto -> producto.getStock() > 0).count()
      );
      model.addAttribute(
          "productosAgotados",
          productos.stream().filter(producto -> producto.getStock() <= 0).count()
      );
    } catch (InventarioNoDisponibleException e) {
      response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
      model.addAttribute("productos", List.of());
      model.addAttribute("productosDisponibles", 0);
      model.addAttribute("productosAgotados", 0);
      model.addAttribute("errorCatalogo", e.getMessage());
    }
    return "supervision-productos";
  }
}
