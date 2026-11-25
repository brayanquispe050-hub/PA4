package com.isil.edu.pe.pedidos.reportes.service;

import com.isil.edu.pe.pedidos.reportes.service.domain.Pedido;
import com.isil.edu.pe.pedidos.reportes.service.domain.ProductoVendido;
import com.isil.edu.pe.pedidos.reportes.service.domain.ResumenPedidos;
import com.isil.edu.pe.pedidos.reportes.service.domain.EstadisticasRevisiones;
import com.isil.edu.pe.pedidos.reportes.service.domain.PedidoRevisionReporte;
import java.util.List;

public interface PedidoReporteService {

  Pedido obtenerPorId(long id);

  List<Pedido> listarTodos();

  ResumenPedidos obtenerResumen();

  List<Pedido> buscarPorCliente(String cliente);

  List<ProductoVendido> listarProductosMasVendidos();

  List<PedidoRevisionReporte> listarRevisiones(String estado);

  EstadisticasRevisiones obtenerEstadisticasRevisiones();

}
