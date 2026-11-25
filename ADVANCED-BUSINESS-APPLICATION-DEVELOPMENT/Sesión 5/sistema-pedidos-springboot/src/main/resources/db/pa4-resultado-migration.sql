-- Apply once to a PA4 database whose revisado column already exists.
ALTER TABLE pedido
    ADD COLUMN estado_revision VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE';

UPDATE pedido
SET estado_revision = CASE WHEN revisado = TRUE THEN 'ACEPTADO' ELSE 'PENDIENTE' END;

ALTER TABLE pedido
    ADD CONSTRAINT chk_pedido_estado_revision
        CHECK (estado_revision IN ('PENDIENTE', 'ACEPTADO', 'RECHAZADO'));
