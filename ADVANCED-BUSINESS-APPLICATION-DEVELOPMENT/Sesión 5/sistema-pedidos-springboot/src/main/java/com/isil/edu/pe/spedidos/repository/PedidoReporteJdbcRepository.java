package com.isil.edu.pe.spedidos.repository;

import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.repository.projection.ProductoVendidoProjection;
import com.isil.edu.pe.spedidos.repository.projection.ResumenPedidosProjection;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PedidoReporteJdbcRepository {

  private static final String COLUMNAS_PEDIDO = """
      id, cliente, producto_codigo, producto_nombre, precio_unitario,
      cantidad, total, fecha, revisado
      """;

  private final JdbcTemplate jdbcTemplate;
  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
  private final RowMapper<Pedido> pedidoRowMapper = (rs, rowNum) -> {
    Timestamp fecha = rs.getTimestamp("fecha");
    Pedido pedido = new Pedido(
        rs.getLong("id"),
        rs.getString("cliente"),
        rs.getString("producto_codigo"),
        rs.getString("producto_nombre"),
        rs.getBigDecimal("precio_unitario"),
        rs.getInt("cantidad"),
        rs.getBigDecimal("total"),
        fecha == null ? null : fecha.toLocalDateTime()
    );
    pedido.setRevisado(rs.getBoolean("revisado"));
    return pedido;
  };

  public PedidoReporteJdbcRepository(
      JdbcTemplate jdbcTemplate,
      NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }

  public ResumenPedidosProjection obtenerResumen() {
    return jdbcTemplate.queryForObject(
        """
        SELECT COUNT(*) AS total_pedidos,
               COALESCE(SUM(cantidad), 0) AS total_unidades,
               COALESCE(SUM(total), 0) AS importe_acumulado,
               COALESCE(AVG(total), 0) AS ticket_promedio
        FROM pedido
        """,
        (rs, rowNum) -> new ResumenPedidosProjection(
            rs.getLong("total_pedidos"),
            rs.getLong("total_unidades"),
            valor(rs.getBigDecimal("importe_acumulado")),
            valor(rs.getBigDecimal("ticket_promedio"))
        )
    );
  }

  public List<Pedido> buscarPorCliente(String cliente) {
    if (cliente == null || cliente.isBlank()) {
      return List.of();
    }
    return namedParameterJdbcTemplate.query(
        "SELECT " + COLUMNAS_PEDIDO
            + " FROM pedido WHERE LOWER(cliente) LIKE LOWER(:cliente) ORDER BY id DESC",
        Map.of("cliente", "%" + cliente.trim() + "%"),
        pedidoRowMapper
    );
  }

  public List<ProductoVendidoProjection> listarProductosMasVendidos() {
    return jdbcTemplate.query(
        """
        SELECT producto_codigo, producto_nombre,
               COUNT(*) AS total_pedidos,
               SUM(cantidad) AS total_unidades,
               SUM(total) AS importe_acumulado
        FROM pedido
        GROUP BY producto_codigo, producto_nombre
        ORDER BY total_unidades DESC, importe_acumulado DESC
        """,
        (rs, rowNum) -> new ProductoVendidoProjection(
            rs.getString("producto_codigo"),
            rs.getString("producto_nombre"),
            rs.getLong("total_pedidos"),
            rs.getLong("total_unidades"),
            valor(rs.getBigDecimal("importe_acumulado"))
        )
    );
  }

  private BigDecimal valor(BigDecimal valor) {
    return valor == null ? BigDecimal.ZERO : valor;
  }
}
