package com.isil.edu.pe.spedidos.exception;

public class PedidoRevisionNoEncontradaException extends RuntimeException {

  public PedidoRevisionNoEncontradaException(long pedidoId) {
    super("No existe el pedido con ID: " + pedidoId);
  }
}
