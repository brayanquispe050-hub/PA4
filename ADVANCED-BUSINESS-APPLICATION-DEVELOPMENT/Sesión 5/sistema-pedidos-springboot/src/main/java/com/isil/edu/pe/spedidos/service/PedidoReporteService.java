package com.isil.edu.pe.spedidos.service;

import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.repository.projection.ProductoVendidoProjection;
import com.isil.edu.pe.spedidos.repository.projection.ResumenPedidosProjection;
import java.util.List;

public interface PedidoReporteService {

  ResumenPedidosProjection obtenerResumen();

  List<Pedido> buscarPorCliente(String cliente);

  List<ProductoVendidoProjection> listarProductosMasVendidos();
}
