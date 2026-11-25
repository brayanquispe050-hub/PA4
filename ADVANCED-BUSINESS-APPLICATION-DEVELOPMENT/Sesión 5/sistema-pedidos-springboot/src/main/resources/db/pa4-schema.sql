-- Apply once to an existing MySQL database if Hibernate schema update is disabled.
ALTER TABLE pedido
    ADD COLUMN revisado BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE pedido
    ADD COLUMN estado_revision VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE';

ALTER TABLE pedido
    ADD CONSTRAINT chk_pedido_estado_revision
        CHECK (estado_revision IN ('PENDIENTE', 'ACEPTADO', 'RECHAZADO'));

CREATE TABLE pedido_revision (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pedido_id BIGINT NOT NULL,
    observacion VARCHAR(500) NOT NULL,
    revisado_por VARCHAR(100) NOT NULL,
    fecha_revision TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_pedido_revision PRIMARY KEY (id),
    CONSTRAINT uq_pedido_revision_pedido UNIQUE (pedido_id),
    CONSTRAINT fk_pedido_revision_pedido
        FOREIGN KEY (pedido_id) REFERENCES pedido (id)
);
