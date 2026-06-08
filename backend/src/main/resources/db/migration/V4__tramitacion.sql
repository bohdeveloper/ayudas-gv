-- ============================================================
-- V4: Tabla de tramitación + índices
-- Fase 1
-- ============================================================

CREATE TABLE tramite (
    id              BIGINT       NOT NULL GENERATED ALWAYS AS IDENTITY,
    expediente_id   BIGINT       NOT NULL,
    tipo_tramite_id INTEGER      NOT NULL,
    fecha           TIMESTAMP    NOT NULL,
    usuario_id      BIGINT       NOT NULL,
    resultado       VARCHAR(30),           -- FAVORABLE | DESFAVORABLE
    observaciones   VARCHAR(1000),
    CONSTRAINT pk_tramite    PRIMARY KEY (id),
    CONSTRAINT fk_tr_exp     FOREIGN KEY (expediente_id)   REFERENCES expediente (id),
    CONSTRAINT fk_tr_tipo    FOREIGN KEY (tipo_tramite_id) REFERENCES tipo_tramite (id),
    CONSTRAINT fk_tr_usuario FOREIGN KEY (usuario_id)      REFERENCES usuario (id)
);

CREATE INDEX idx_tramite_expediente ON tramite (expediente_id);
CREATE INDEX idx_tramite_fecha      ON tramite (fecha);
