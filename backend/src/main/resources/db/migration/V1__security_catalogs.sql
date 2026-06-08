-- ============================================================
-- V1: Tablas de catálogo + seguridad + datos semilla
-- Semana 1 — Fase 0
-- ============================================================

-- Catálogo de tipos de ayuda
CREATE TABLE tipo_ayuda (
    id             INTEGER      NOT NULL,
    codigo         VARCHAR(20)  NOT NULL,
    descripcion_es VARCHAR(255) NOT NULL,
    descripcion_eu VARCHAR(255) NOT NULL,
    modalidad_pago VARCHAR(20)  NOT NULL,  -- PERIODICO | PAGO_UNICO
    activo         BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_tipo_ayuda       PRIMARY KEY (id),
    CONSTRAINT uq_tipo_ayuda_codigo UNIQUE (codigo)
);

-- Catálogo de estados del expediente
CREATE TABLE estado_expediente (
    id             INTEGER      NOT NULL,
    codigo         VARCHAR(30)  NOT NULL,
    descripcion_es VARCHAR(255) NOT NULL,
    descripcion_eu VARCHAR(255) NOT NULL,
    orden          INTEGER,
    CONSTRAINT pk_estado_expediente       PRIMARY KEY (id),
    CONSTRAINT uq_estado_expediente_codigo UNIQUE (codigo)
);

-- Catálogo de tipos de trámite
CREATE TABLE tipo_tramite (
    id             INTEGER      NOT NULL,
    codigo         VARCHAR(40)  NOT NULL,
    descripcion_es VARCHAR(255) NOT NULL,
    descripcion_eu VARCHAR(255) NOT NULL,
    CONSTRAINT pk_tipo_tramite       PRIMARY KEY (id),
    CONSTRAINT uq_tipo_tramite_codigo UNIQUE (codigo)
);

-- Usuarios internos del sistema
CREATE TABLE usuario (
    id               BIGINT       NOT NULL GENERATED ALWAYS AS IDENTITY,
    username         VARCHAR(60)  NOT NULL,
    password_hash    VARCHAR(255) NOT NULL,
    nombre           VARCHAR(100) NOT NULL,
    apellidos        VARCHAR(150) NOT NULL,
    email            VARCHAR(150),
    idioma_preferido VARCHAR(5)   NOT NULL DEFAULT 'es',
    activo           BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_alta       TIMESTAMP    NOT NULL,
    CONSTRAINT pk_usuario          PRIMARY KEY (id),
    CONSTRAINT uq_usuario_username UNIQUE (username),
    CONSTRAINT ck_usuario_idioma   CHECK (idioma_preferido IN ('es', 'eu'))
);

-- Roles del sistema
CREATE TABLE rol (
    id             INTEGER      NOT NULL,
    codigo         VARCHAR(40)  NOT NULL,
    descripcion_es VARCHAR(255) NOT NULL,
    descripcion_eu VARCHAR(255) NOT NULL,
    CONSTRAINT pk_rol       PRIMARY KEY (id),
    CONSTRAINT uq_rol_codigo UNIQUE (codigo)
);

-- Asignación de roles a usuarios (N:M)
CREATE TABLE usuario_rol (
    usuario_id BIGINT  NOT NULL,
    rol_id     INTEGER NOT NULL,
    CONSTRAINT pk_usuario_rol PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT fk_ur_usuario  FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT fk_ur_rol      FOREIGN KEY (rol_id)     REFERENCES rol (id)
);

-- ────────────────────────────────────────────────────────────
-- DATOS SEMILLA
-- ────────────────────────────────────────────────────────────

INSERT INTO tipo_ayuda (id, codigo, descripcion_es, descripcion_eu, modalidad_pago, activo) VALUES
    (1, 'ASIG_DISCAP',
        'Asignación económica por hijo o menor a cargo con discapacidad',
        'Desgaitasuna duen seme-alaba edo ardurapeko adingaberako diru-laguntza',
        'PERIODICO', TRUE),
    (2, 'PAGO_NAC',
        'Pago único por nacimiento o adopción (supuestos específicos)',
        'Jaiotza edo adopzioagatiko ordainketa bakarra (kasu espezifikoak)',
        'PAGO_UNICO', TRUE),
    (3, 'PARTO_MULT',
        'Pago único por parto o adopción múltiples',
        'Erditze edo adopzio anitzengatiko ordainketa bakarra',
        'PAGO_UNICO', TRUE);

INSERT INTO estado_expediente (id, codigo, descripcion_es, descripcion_eu, orden) VALUES
    (1, 'REGISTRADO',  'Registrado',            'Erregistratuta',          1),
    (2, 'ASIGNADO',    'Asignado a instructor',  'Instruktoreari esleituta', 2),
    (3, 'EN_REVISION', 'En revisión',            'Berrikusten',             3),
    (4, 'RESUELTO',    'Resuelto',               'Ebatzita',                4);

INSERT INTO tipo_tramite (id, codigo, descripcion_es, descripcion_eu) VALUES
    (1, 'REGISTRO',     'Registro de entrada',      'Sarrera-erregistroa'),
    (2, 'ASIGNACION',   'Asignación de instructor', 'Instruktorea esleitzea'),
    (3, 'REVISION',     'Revisión / subsanación',   'Berrikuspena / zuzenketa'),
    (4, 'PROPUESTA',    'Propuesta de resolución',  'Ebazpen-proposamena'),
    (5, 'RESOLUCION',   'Resolución',               'Ebazpena'),
    (6, 'NOTIFICACION', 'Notificación',             'Jakinarazpena');

INSERT INTO rol (id, codigo, descripcion_es, descripcion_eu) VALUES
    (1, 'CONSULTOR',   'Búsqueda y consulta de expedientes',   'Espedienteak bilatu eta kontsultatzea'),
    (2, 'INSTRUCTOR',  'Tramitación de expedientes asignados', 'Esleitutako espedienteak izapidetzea'),
    (3, 'RESPONSABLE', 'Asignación y resolución',              'Esleipena eta ebazpena'),
    (4, 'ADMIN',       'Administración del sistema',           'Sistemaren administrazioa');

-- Contraseña de todos los usuarios de prueba: admin123
-- Hash BCrypt de "admin123" generado con strength 10
INSERT INTO usuario (username, password_hash, nombre, apellidos, email, idioma_preferido, activo, fecha_alta) VALUES
    ('admin',       '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaIW0B9hd2LTa',
                    'Administrador', 'Sistema',    'admin@ayudasgv.eus',       'es', TRUE, NOW()),
    ('instructor1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaIW0B9hd2LTa',
                    'Ane',           'Etxeberria', 'aetxeberria@ayudasgv.eus', 'eu', TRUE, NOW()),
    ('consultor1',  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaIW0B9hd2LTa',
                    'Mikel',         'Zabala',     'mzabala@ayudasgv.eus',     'es', TRUE, NOW());

INSERT INTO usuario_rol (usuario_id, rol_id)
    SELECT u.id, r.id FROM usuario u, rol r WHERE u.username = 'admin'       AND r.codigo = 'ADMIN';
INSERT INTO usuario_rol (usuario_id, rol_id)
    SELECT u.id, r.id FROM usuario u, rol r WHERE u.username = 'admin'       AND r.codigo = 'RESPONSABLE';
INSERT INTO usuario_rol (usuario_id, rol_id)
    SELECT u.id, r.id FROM usuario u, rol r WHERE u.username = 'instructor1' AND r.codigo = 'INSTRUCTOR';
INSERT INTO usuario_rol (usuario_id, rol_id)
    SELECT u.id, r.id FROM usuario u, rol r WHERE u.username = 'consultor1'  AND r.codigo = 'CONSULTOR';
