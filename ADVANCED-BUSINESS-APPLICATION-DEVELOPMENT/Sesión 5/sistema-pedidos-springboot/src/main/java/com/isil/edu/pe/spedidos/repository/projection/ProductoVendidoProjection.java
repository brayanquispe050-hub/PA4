package com.isil.edu.pe.spedidos.repository.projection;

import java.math.BigDecimal;

public record ProductoVendidoProjection(
    String productoCodigo,
    String productoNombre,
    long totalPedidos,
    long totalUnidades,
    BigDecimal importeAcumulado
) {
}
