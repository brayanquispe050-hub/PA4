package com.isil.edu.pe.spedidos.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PedidoRevisionJdbcRepository {

  private final JdbcTemplate jdbcTemplate;

  public PedidoRevisionJdbcRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public int marcarComoRevisado(long pedidoId, String resultado) {
    return jdbcTemplate.update(
        "UPDATE pedido SET revisado = ?, estado_revision = ? WHERE id = ? AND revisado = ?",
        true,
        resultado,
        pedidoId,
        false
    );
  }

  public void registrarRevision(long pedidoId, String observacion, String revisadoPor) {
    jdbcTemplate.update(
        """
        INSERT INTO pedido_revision (pedido_id, observacion, revisado_por)
        VALUES (?, ?, ?)
        """,
        pedidoId,
        observacion,
        revisadoPor
    );
  }
}
