package com.isil.edu.pe.spedidos.repository.projection;

import java.math.BigDecimal;

public record ResumenPedidosProjection(
    long totalPedidos,
    long totalUnidades,
    BigDecimal importeAcumulado,
    BigDecimal ticketPromedio
) {
}
