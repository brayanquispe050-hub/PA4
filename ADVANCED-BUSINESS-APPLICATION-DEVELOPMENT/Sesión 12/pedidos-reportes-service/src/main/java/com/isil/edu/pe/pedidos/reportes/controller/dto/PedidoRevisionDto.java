package com.isil.edu.pe.pedidos.reportes.controller.dto;

import com.isil.edu.pe.pedidos.reportes.service.domain.PedidoRevisionReporte;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoRevisionDto(
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

  public static PedidoRevisionDto from(PedidoRevisionReporte reporte) {
    return new PedidoRevisionDto(
        reporte.pedidoId(),
        reporte.cliente(),
        reporte.productoCodigo(),
        reporte.productoNombre(),
        reporte.total(),
        reporte.estado(),
        reporte.resultado(),
        reporte.observacion(),
        reporte.revisadoPor(),
        reporte.fechaRevision()
    );
  }
}
