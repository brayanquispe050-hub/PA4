-- Apply once to a PA4 database whose pedido_revision table predates observacion.
ALTER TABLE pedido_revision
    ADD COLUMN observacion VARCHAR(500) NOT NULL DEFAULT '';
