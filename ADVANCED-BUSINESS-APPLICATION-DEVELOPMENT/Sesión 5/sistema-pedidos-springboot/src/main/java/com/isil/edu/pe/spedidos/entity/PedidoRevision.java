package com.isil.edu.pe.spedidos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "pedido_revision",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_pedido_revision_pedido",
        columnNames = "pedido_id"
    )
)
public class PedidoRevision {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "pedido_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_pedido_revision_pedido")
  )
  private Pedido pedido;

  @Column(
      name = "observacion",
      nullable = false,
      length = 500,
      columnDefinition = "VARCHAR(500) NOT NULL DEFAULT ''"
  )
  private String observacion;

  @Column(name = "revisado_por", nullable = false, length = 100)
  private String revisadoPor;

  @Column(
      name = "fecha_revision",
      nullable = false,
      columnDefinition = "timestamp default current_timestamp"
  )
  private LocalDateTime fechaRevision;

  protected PedidoRevision() {
  }

  public PedidoRevision(
      Pedido pedido,
      String observacion,
      String revisadoPor,
      LocalDateTime fechaRevision) {
    this.pedido = pedido;
    this.observacion = observacion;
    this.revisadoPor = revisadoPor;
    this.fechaRevision = fechaRevision;
  }
}
