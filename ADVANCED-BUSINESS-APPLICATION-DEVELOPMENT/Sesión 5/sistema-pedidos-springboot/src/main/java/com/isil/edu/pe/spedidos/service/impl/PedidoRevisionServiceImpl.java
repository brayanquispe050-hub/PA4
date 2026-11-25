package com.isil.edu.pe.spedidos.service.impl;

import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.exception.PedidoRevisionNoEncontradaException;
import com.isil.edu.pe.spedidos.exception.PedidoYaRevisadoException;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;
import com.isil.edu.pe.spedidos.repository.PedidoRevisionJdbcRepository;
import com.isil.edu.pe.spedidos.service.PedidoRevisionService;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoRevisionServiceImpl implements PedidoRevisionService {

  private final PedidoRepository pedidoRepository;
  private final PedidoRevisionJdbcRepository revisionRepository;

  public PedidoRevisionServiceImpl(
      PedidoRepository pedidoRepository,
      PedidoRevisionJdbcRepository revisionRepository) {
    this.pedidoRepository = pedidoRepository;
    this.revisionRepository = revisionRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Pedido> listarPendientes() {
    return pedidoRepository.findAllByRevisadoFalseOrderByIdDesc();
  }

  @Override
  @Transactional
  public void revisarPedido(
      long pedidoId,
      String resultado,
      String observacion,
      String revisadoPor) {
    if (pedidoId <= 0) {
      throw new IllegalArgumentException("El ID del pedido debe ser mayor que cero.");
    }
    if (!"ACEPTADO".equals(resultado) && !"RECHAZADO".equals(resultado)) {
      throw new IllegalArgumentException("El resultado debe ser ACEPTADO o RECHAZADO.");
    }
    if (observacion == null || observacion.isBlank() || observacion.trim().length() > 500) {
      throw new IllegalArgumentException("La observación debe tener entre 1 y 500 caracteres.");
    }
    if (revisadoPor == null || revisadoPor.isBlank() || revisadoPor.length() > 100) {
      throw new IllegalArgumentException("El usuario revisor debe tener entre 1 y 100 caracteres.");
    }
    if (!pedidoRepository.existsById(pedidoId)) {
      throw new PedidoRevisionNoEncontradaException(pedidoId);
    }
    if (revisionRepository.marcarComoRevisado(pedidoId, resultado) != 1) {
      if (!pedidoRepository.existsById(pedidoId)) {
        throw new PedidoRevisionNoEncontradaException(pedidoId);
      }
      throw new PedidoYaRevisadoException(pedidoId);
    }

    try {
      revisionRepository.registrarRevision(
          pedidoId,
          observacion.trim(),
          revisadoPor.trim()
      );
    } catch (DuplicateKeyException e) {
      throw new PedidoYaRevisadoException(pedidoId);
    }
  }
}
