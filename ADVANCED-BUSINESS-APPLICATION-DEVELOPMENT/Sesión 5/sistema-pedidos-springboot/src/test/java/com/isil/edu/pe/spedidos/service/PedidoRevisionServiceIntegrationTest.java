package com.isil.edu.pe.spedidos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.isil.edu.pe.spedidos.exception.PedidoRevisionNoEncontradaException;
import com.isil.edu.pe.spedidos.exception.PedidoYaRevisadoException;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PedidoRevisionServiceIntegrationTest {

  @Autowired
  private PedidoRevisionService revisionService;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private PedidoRepository pedidoRepository;

  private long pedidoId;

  @BeforeEach
  void prepararPedidoPendiente() {
    jdbcTemplate.update("DELETE FROM pedido_revision");
    jdbcTemplate.update("DELETE FROM pedido");
    jdbcTemplate.update(
        """
        INSERT INTO pedido (
          cliente, producto_codigo, producto_nombre, precio_unitario,
          cantidad, total, fecha, revisado
        ) VALUES ('Ana Torres', 'LAP-001', 'Laptop', 100.00, 1, 100.00, CURRENT_TIMESTAMP, FALSE)
        """
    );
    pedidoId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM pedido", Long.class);
  }

  @Test
  void commitsPedidoFlagAndUniqueReviewRecord() {
    revisionService.revisarPedido(pedidoId, "ACEPTADO", "Pedido verificado.", "operador");

    assertTrue(pedidoRevisado());
    assertEquals(1, contarRevisiones());
    assertEquals("Pedido verificado.", jdbcTemplate.queryForObject(
        "SELECT observacion FROM pedido_revision WHERE pedido_id = ?",
        String.class,
        pedidoId
    ));
    assertEquals("ACEPTADO", jdbcTemplate.queryForObject(
        "SELECT estado_revision FROM pedido WHERE id = ?",
        String.class,
        pedidoId
    ));
    assertEquals("operador", jdbcTemplate.queryForObject(
        "SELECT revisado_por FROM pedido_revision WHERE pedido_id = ?",
        String.class,
        pedidoId
    ));
  }

  @Test
  void commitsRejectedResultWithItsAudit() {
    revisionService.revisarPedido(
        pedidoId,
        "RECHAZADO",
        "Falta validar el precio.",
        "operador"
    );

    assertTrue(pedidoRevisado());
    assertEquals("RECHAZADO", jdbcTemplate.queryForObject(
        "SELECT estado_revision FROM pedido WHERE id = ?",
        String.class,
        pedidoId
    ));
    assertEquals("Falta validar el precio.", jdbcTemplate.queryForObject(
        "SELECT observacion FROM pedido_revision WHERE pedido_id = ?",
        String.class,
        pedidoId
    ));
  }

  @Test
  void orderListingLoadsTheDecisionAlongsideThePedido() {
    revisionService.revisarPedido(
        pedidoId,
        "RECHAZADO",
        "Falta validar el precio.",
        "operador"
    );

    assertEquals("RECHAZADO", pedidoRepository.findAllByOrderByIdDesc().get(0).getEstadoRevision());
  }

  @Test
  void rollsBackPedidoFlagWhenReviewInsertFails() {
    jdbcTemplate.execute("""
        ALTER TABLE pedido_revision
        ADD CONSTRAINT chk_revision_rollback CHECK (revisado_por <> 'rollback-user')
        """);

    assertThrows(
        DataIntegrityViolationException.class,
        () -> revisionService.revisarPedido(
            pedidoId,
            "RECHAZADO",
            "Falla forzada.",
            "rollback-user"
        )
    );

    assertFalse(pedidoRevisado());
    assertEquals(0, contarRevisiones());
    jdbcTemplate.execute("ALTER TABLE pedido_revision DROP CONSTRAINT chk_revision_rollback");
  }

  @Test
  void rejectsMissingAndAlreadyReviewedOrders() {
    assertThrows(
        PedidoRevisionNoEncontradaException.class,
        () -> revisionService.revisarPedido(
            pedidoId + 100,
            "ACEPTADO",
            "Revisión.",
            "operador"
        )
    );

    revisionService.revisarPedido(pedidoId, "ACEPTADO", "Revisión inicial.", "operador");
    assertThrows(
        PedidoYaRevisadoException.class,
        () -> revisionService.revisarPedido(
            pedidoId,
            "RECHAZADO",
            "Segunda revisión.",
            "operador"
        )
    );
    assertEquals(1, contarRevisiones());
  }

  @Test
  void rejectsInvalidIdAndBlankObservationWithoutWriting() {
    assertThrows(
        IllegalArgumentException.class,
        () -> revisionService.revisarPedido(0, "ACEPTADO", "Observación.", "operador")
    );
    assertThrows(
        IllegalArgumentException.class,
        () -> revisionService.revisarPedido(pedidoId, "ACEPTADO", "   ", "operador")
    );

    assertFalse(pedidoRevisado());
    assertEquals(0, contarRevisiones());
  }

  @Test
  void rejectsUnsupportedReviewResult() {
    assertThrows(
        IllegalArgumentException.class,
        () -> revisionService.revisarPedido(pedidoId, "PENDIENTE", "Observación.", "operador")
    );
    assertFalse(pedidoRevisado());
    assertEquals(0, contarRevisiones());
  }

  @Test
  void enforcesReviewForeignKeyAndUniquePedidoConstraint() {
    jdbcTemplate.update(
        "INSERT INTO pedido_revision (pedido_id, observacion, revisado_por) VALUES (?, ?, ?)",
        pedidoId,
        "Revisión de prueba.",
        "operador"
    );

    assertThrows(
        DataIntegrityViolationException.class,
        () -> jdbcTemplate.update(
            "INSERT INTO pedido_revision (pedido_id, observacion, revisado_por) VALUES (?, ?, ?)",
            pedidoId,
            "Segunda revisión.",
            "otro-operador"
        )
    );
    assertThrows(
        DataIntegrityViolationException.class,
        () -> jdbcTemplate.update(
            "INSERT INTO pedido_revision (pedido_id, observacion, revisado_por) VALUES (?, ?, ?)",
            pedidoId + 100,
            "ID inexistente.",
            "operador"
        )
    );
    assertEquals(1, contarRevisiones());
  }

  private boolean pedidoRevisado() {
    return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
        "SELECT revisado FROM pedido WHERE id = ?",
        Boolean.class,
        pedidoId
    ));
  }

  private int contarRevisiones() {
    return jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM pedido_revision WHERE pedido_id = ?",
        Integer.class,
        pedidoId
    );
  }
}
