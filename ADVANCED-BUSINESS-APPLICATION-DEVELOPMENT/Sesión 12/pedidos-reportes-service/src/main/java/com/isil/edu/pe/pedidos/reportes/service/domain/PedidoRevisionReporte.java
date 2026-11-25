package com.isil.edu.pe.pedidos.reportes.service.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoRevisionReporte(
    long pedidoId,
    String cliente,
    String productoCodigo,
    String productoNombre,
    BigDecimal total,
    String estado,
    String resultado,
    String observacion,
    String revisadoPor,
    LocalDateTime fechaRevision
) {
}
