package com.isil.edu.pe.spedidos.exception;

public class PedidoYaRevisadoException extends RuntimeException {

  public PedidoYaRevisadoException(long pedidoId) {
    super("El pedido con ID " + pedidoId + " ya fue revisado.");
  }
}
