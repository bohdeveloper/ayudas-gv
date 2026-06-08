-- ============================================================
-- V2: Tablas del expediente y sus secciones + datos de ejemplo
-- Semana 4 — Fase 0
-- ============================================================

-- Personas físicas (solicitantes, representantes, causantes)
CREATE TABLE persona (
    id               BIGINT       NOT NULL GENERATED ALWAYS AS IDENTITY,
    tipo_documento   VARCHAR(10)  NOT NULL,  -- DNI | NIE | PAS
    numero_documento VARCHAR(20)  NOT NULL,
    nombre           VARCHAR(100) NOT NULL,
    apellido1        VARCHAR(100) NOT NULL,
    apellido2        VARCHAR(100),
    fecha_nacimiento DATE,
    sexo             VARCHAR(1),
    CONSTRAINT pk_persona     PRIMARY KEY (id),
    CONSTRAINT uq_persona_doc UNIQUE (tipo_documento, numero_documento)
);

-- Entidad central del sistema
CREATE TABLE expediente (
    id                BIGINT      NOT NULL GENERATED ALWAYS AS IDENTITY,
    numero_expediente VARCHAR(30) NOT NULL,
    tipo_ayuda_id     INTEGER     NOT NULL,
    estado_id         INTEGER     NOT NULL,
    fecha_registro    TIMESTAMP   NOT NULL,
    instructor_id     BIGINT,
    fecha_resolucion  DATE,
    CONSTRAINT pk_expediente        PRIMARY KEY (id),
    CONSTRAINT uq_expediente_numero UNIQUE (numero_expediente),
    CONSTRAINT fk_exp_tipo          FOREIGN KEY (tipo_ayuda_id) REFERENCES tipo_ayuda (id),
    CONSTRAINT fk_exp_estado        FOREIGN KEY (estado_id)     REFERENCES estado_expediente (id),
    CONSTRAINT fk_exp_instructor    FOREIGN KEY (instructor_id) REFERENCES usuario (id)
);

-- Personas vinculadas al expediente según su rol
CREATE TABLE expediente_interesado (
    id             BIGINT      NOT NULL GENERATED ALWAYS AS IDENTITY,
    expediente_id  BIGINT      NOT NULL,
    persona_id     BIGINT      NOT NULL,
    rol_interesado VARCHAR(20) NOT NULL,  -- SOLICITANTE | REPRESENTANTE | CAUSANTE
    CONSTRAINT pk_exp_int     PRIMARY KEY (id),
    CONSTRAINT fk_ei_exp      FOREIGN KEY (expediente_id) REFERENCES expediente (id),
    CONSTRAINT fk_ei_persona  FOREIGN KEY (persona_id)    REFERENCES persona (id),
    CONSTRAINT uq_ei UNIQUE (expediente_id, persona_id, rol_interesado)
);

-- Sección de contacto y aviso (1:1 con expediente)
CREATE TABLE datos_contacto (
    id                      BIGINT      NOT NULL GENERATED ALWAYS AS IDENTITY,
    expediente_id           BIGINT      NOT NULL,
    via                     VARCHAR(150),
    numero                  VARCHAR(10),
    piso                    VARCHAR(10),
    codigo_postal           VARCHAR(10),
    municipio               VARCHAR(100),
    provincia               VARCHAR(100),
    telefono                VARCHAR(20),
    email                   VARCHAR(150),
    canal_aviso             VARCHAR(20),  -- EMAIL | SMS | POSTAL
    idioma_notificacion     VARCHAR(5),   -- es | eu (independiente del idioma de interfaz)
    acepta_notif_telematica BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_datos_contacto PRIMARY KEY (id),
    CONSTRAINT fk_dc_exp         FOREIGN KEY (expediente_id) REFERENCES expediente (id),
    CONSTRAINT uq_dc_exp         UNIQUE (expediente_id),
    CONSTRAINT ck_dc_idioma      CHECK (idioma_notificacion IN ('es', 'eu'))
);

-- Sección de datos de la solicitud (1:1 con expediente)
CREATE TABLE solicitud_ayuda (
    id                 BIGINT        NOT NULL GENERATED ALWAYS AS IDENTITY,
    expediente_id      BIGINT        NOT NULL,
    fecha_solicitud    DATE          NOT NULL,
    importe_solicitado DECIMAL(12,2),
    observaciones      VARCHAR(1000),
    CONSTRAINT pk_solicitud PRIMARY KEY (id),
    CONSTRAINT fk_sol_exp   FOREIGN KEY (expediente_id) REFERENCES expediente (id),
    CONSTRAINT uq_sol_exp   UNIQUE (expediente_id)
);

-- Detalle Ayuda 1: asignación por hijo/menor con discapacidad
CREATE TABLE detalle_asignacion_discapacidad (
    solicitud_id             BIGINT  NOT NULL,
    causante_persona_id      BIGINT  NOT NULL,
    grado_discapacidad       INTEGER NOT NULL,
    necesita_tercera_persona BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_det_asig   PRIMARY KEY (solicitud_id, causante_persona_id),
    CONSTRAINT fk_da_sol     FOREIGN KEY (solicitud_id)        REFERENCES solicitud_ayuda (id),
    CONSTRAINT fk_da_persona FOREIGN KEY (causante_persona_id) REFERENCES persona (id),
    CONSTRAINT ck_da_grado   CHECK (grado_discapacidad BETWEEN 0 AND 100)
);

-- Detalle Ayuda 2: pago único nacimiento/adopción
CREATE TABLE detalle_nacimiento_adopcion (
    solicitud_id       BIGINT        NOT NULL,
    supuesto           VARCHAR(20)   NOT NULL,  -- FAMILIA_NUMEROSA | MONOPARENTAL | DISCAPACIDAD
    fecha_hecho        DATE          NOT NULL,
    ingresos_anuales   DECIMAL(12,2),
    numero_hijos_cargo INTEGER,
    CONSTRAINT pk_det_nac PRIMARY KEY (solicitud_id),
    CONSTRAINT fk_dna_sol FOREIGN KEY (solicitud_id) REFERENCES solicitud_ayuda (id)
);

-- Detalle Ayuda 3: parto o adopción múltiples
CREATE TABLE detalle_parto_multiple (
    solicitud_id   BIGINT  NOT NULL,
    numero_nacidos INTEGER NOT NULL,  -- >= 2; cuantía: 4/8/12 × SMI
    fecha_hecho    DATE    NOT NULL,
    CONSTRAINT pk_det_parto PRIMARY KEY (solicitud_id),
    CONSTRAINT fk_dpm_sol   FOREIGN KEY (solicitud_id) REFERENCES solicitud_ayuda (id),
    CONSTRAINT ck_dpm_num   CHECK (numero_nacidos >= 2)
);

-- ────────────────────────────────────────────────────────────
-- DATOS DE EJEMPLO
-- ────────────────────────────────────────────────────────────

INSERT INTO persona (tipo_documento, numero_documento, nombre, apellido1, apellido2, fecha_nacimiento, sexo) VALUES
    ('DNI', '12345678A', 'María',  'García',    'López',   '1985-03-15', 'F'),
    ('DNI', '87654321B', 'Jon',    'Etxeberria','Urrutia', '1990-07-22', 'M'),
    ('DNI', '11223344C', 'Ainhoa', 'Zubikarai', 'Mendez',  '2010-11-05', 'F'),
    ('DNI', '44332211D', 'Unai',   'Iturriaga', 'Bilbao',  '2015-02-18', 'M'),
    ('NIE', 'X1234567Z', 'Ana',    'Fernández', 'Martín',  '1992-06-30', 'F'),
    ('DNI', '99887766E', 'Iker',   'Beristain', 'Osa',     '2022-04-10', 'M');

INSERT INTO expediente (numero_expediente, tipo_ayuda_id, estado_id, fecha_registro, instructor_id) VALUES
    ('EXP-2024-001', 1, 2, '2024-01-10 09:00:00', (SELECT id FROM usuario WHERE username = 'instructor1')),
    ('EXP-2024-002', 2, 1, '2024-01-15 10:30:00', NULL),
    ('EXP-2024-003', 3, 3, '2024-01-20 11:00:00', (SELECT id FROM usuario WHERE username = 'instructor1')),
    ('EXP-2024-004', 1, 1, '2024-02-01 08:45:00', NULL),
    ('EXP-2024-005', 2, 4, '2024-02-10 14:00:00', (SELECT id FROM usuario WHERE username = 'instructor1'));

INSERT INTO expediente_interesado (expediente_id, persona_id, rol_interesado)
    SELECT e.id, p.id, 'SOLICITANTE'
    FROM expediente e, persona p
    WHERE e.numero_expediente = 'EXP-2024-001' AND p.numero_documento = '12345678A';

INSERT INTO expediente_interesado (expediente_id, persona_id, rol_interesado)
    SELECT e.id, p.id, 'CAUSANTE'
    FROM expediente e, persona p
    WHERE e.numero_expediente = 'EXP-2024-001' AND p.numero_documento = '11223344C';

INSERT INTO expediente_interesado (expediente_id, persona_id, rol_interesado)
    SELECT e.id, p.id, 'SOLICITANTE'
    FROM expediente e, persona p
    WHERE e.numero_expediente = 'EXP-2024-002' AND p.numero_documento = '87654321B';

INSERT INTO datos_contacto (expediente_id, via, numero, codigo_postal, municipio, provincia, telefono, email, canal_aviso, idioma_notificacion, acepta_notif_telematica)
    SELECT id, 'Calle Mayor', '5', '20100', 'Errenteria', 'Gipuzkoa', '600111222', 'maria.garcia@email.com', 'EMAIL', 'es', TRUE
    FROM expediente WHERE numero_expediente = 'EXP-2024-001';

INSERT INTO datos_contacto (expediente_id, via, numero, codigo_postal, municipio, provincia, telefono, email, canal_aviso, idioma_notificacion, acepta_notif_telematica)
    SELECT id, 'Kale Nagusia', '12', '20400', 'Tolosa', 'Gipuzkoa', '600333444', 'jon.etxeberria@email.com', 'EMAIL', 'eu', TRUE
    FROM expediente WHERE numero_expediente = 'EXP-2024-002';

INSERT INTO solicitud_ayuda (expediente_id, fecha_solicitud, observaciones)
    SELECT id, '2024-01-10', 'Solicitud presentada en oficina'
    FROM expediente WHERE numero_expediente = 'EXP-2024-001';

INSERT INTO solicitud_ayuda (expediente_id, fecha_solicitud)
    SELECT id, '2024-01-15'
    FROM expediente WHERE numero_expediente = 'EXP-2024-002';

INSERT INTO detalle_asignacion_discapacidad (solicitud_id, causante_persona_id, grado_discapacidad, necesita_tercera_persona)
    SELECT sa.id, p.id, 45, FALSE
    FROM solicitud_ayuda sa
    JOIN expediente e ON sa.expediente_id = e.id
    JOIN persona p ON p.numero_documento = '11223344C'
    WHERE e.numero_expediente = 'EXP-2024-001';

INSERT INTO solicitud_ayuda (expediente_id, fecha_solicitud)
    SELECT id, '2024-01-20'
    FROM expediente WHERE numero_expediente = 'EXP-2024-003';

INSERT INTO detalle_parto_multiple (solicitud_id, numero_nacidos, fecha_hecho)
    SELECT sa.id, 3, '2023-12-01'
    FROM solicitud_ayuda sa
    JOIN expediente e ON sa.expediente_id = e.id
    WHERE e.numero_expediente = 'EXP-2024-003';
