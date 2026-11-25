package com.isil.edu.pe.pedidos.reportes.controller.dto;

import com.isil.edu.pe.pedidos.reportes.service.domain.EstadisticasRevisiones;
import java.math.BigDecimal;

public record EstadisticasRevisionesDto(
    long totalPedidos,
    long totalRevisados,
    long totalPendientes,
    BigDecimal porcentajeRevisado
) {

  public static EstadisticasRevisionesDto from(EstadisticasRevisiones estadisticas) {
    return new EstadisticasRevisionesDto(
        estadisticas.totalPedidos(),
        estadisticas.totalRevisados(),
        estadisticas.totalPendientes(),
        estadisticas.porcentajeRevisado()
    );
  }
}
