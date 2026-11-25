package com.isil.edu.pe.pedidos.reportes.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(PedidoNoEncontradoException.class)
  public ProblemDetail pedidoNoEncontrado(PedidoNoEncontradoException e) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    problem.setTitle("Pedido no encontrado");
    return problem;
  }
}