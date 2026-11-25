package com.isil.edu.pe.spedidos.service.impl;

import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.repository.PedidoReporteJdbcRepository;
import com.isil.edu.pe.spedidos.repository.projection.ProductoVendidoProjection;
import com.isil.edu.pe.spedidos.repository.projection.ResumenPedidosProjection;
import com.isil.edu.pe.spedidos.service.PedidoReporteService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoReporteServiceImpl implements PedidoReporteService {

  private final PedidoReporteJdbcRepository repository;

  public PedidoReporteServiceImpl(PedidoReporteJdbcRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional(readOnly = true)
  public ResumenPedidosProjection obtenerResumen() {
    return repository.obtenerResumen();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Pedido> buscarPorCliente(String cliente) {
    return repository.buscarPorCliente(cliente);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ProductoVendidoProjection> listarProductosMasVendidos() {
    return repository.listarProductosMasVendidos();
  }
}
