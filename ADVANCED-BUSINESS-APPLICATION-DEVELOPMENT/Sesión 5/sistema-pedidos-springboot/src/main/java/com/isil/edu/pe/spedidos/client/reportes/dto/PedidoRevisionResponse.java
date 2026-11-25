package com.isil.edu.pe.spedidos.client.reportes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoRevisionResponse(
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
