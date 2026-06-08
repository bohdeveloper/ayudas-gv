-- ============================================================
-- V3: Tabla de auditoría + índices de rendimiento
-- Semana 6 — Fase 0
-- ============================================================

CREATE TABLE auditoria (
    id         BIGINT       NOT NULL GENERATED ALWAYS AS IDENTITY,
    fecha_hora TIMESTAMP    NOT NULL,
    usuario_id BIGINT,
    username   VARCHAR(60),
    accion     VARCHAR(20)  NOT NULL,   -- LOGIN | READ | CREATE | UPDATE | DELETE | TRAMITE
    entidad    VARCHAR(60)  NOT NULL,   -- EXPEDIENTE | TRAMITE | USUARIO ...
    entidad_id VARCHAR(40),
    detalle    VARCHAR(2000),
    ip_origen  VARCHAR(45),
    CONSTRAINT pk_auditoria PRIMARY KEY (id)
);

-- Índices para rendimiento en búsquedas de expedientes
CREATE INDEX idx_expediente_estado  ON expediente (estado_id);
CREATE INDEX idx_expediente_tipo    ON expediente (tipo_ayuda_id);
CREATE INDEX idx_expediente_fecha   ON expediente (fecha_registro);

-- Índice para búsqueda por documento del solicitante
CREATE INDEX idx_persona_documento  ON persona (numero_documento);

-- Índices de auditoría
CREATE INDEX idx_auditoria_fecha    ON auditoria (fecha_hora);
CREATE INDEX idx_auditoria_usuario  ON auditoria (usuario_id);
