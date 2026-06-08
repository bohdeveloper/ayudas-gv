# Modelo de datos — Sistema de Gestión de Ayudas GV

## Diagrama entidad-relación

![Diagrama ER](assets/er-diagrama.png)

> El color agrupa contextos: catálogos/seguridad (morado), personas (verde), núcleo del expediente (rojo), detalle por tipo de ayuda (naranja), tramitación (azul), auditoría (gris).
> La línea discontinua indica FK opcional o referencia transversal.

---

## Contextos del modelo

### 1. Catálogos y seguridad

**`tipo_ayuda`** — Tipos de ayuda gestionados
```sql
CREATE TABLE tipo_ayuda (
    id             INTEGER      NOT NULL,
    codigo         VARCHAR(20)  NOT NULL,          -- ASIG_DISCAP | PAGO_NAC | PARTO_MULT
    descripcion_es VARCHAR(255) NOT NULL,
    descripcion_eu VARCHAR(255) NOT NULL,
    modalidad_pago VARCHAR(20)  NOT NULL,          -- PERIODICO | PAGO_UNICO
    activo         BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_tipo_ayuda      PRIMARY KEY (id),
    CONSTRAINT uq_tipo_ayuda_codigo UNIQUE (codigo)
);
```

**`estado_expediente`** — Estados del flujo de tramitación
```sql
CREATE TABLE estado_expediente (
    id             INTEGER      NOT NULL,
    codigo         VARCHAR(30)  NOT NULL,          -- REGISTRADO | ASIGNADO | EN_REVISION | RESUELTO
    descripcion_es VARCHAR(255) NOT NULL,
    descripcion_eu VARCHAR(255) NOT NULL,
    orden          INTEGER,
    CONSTRAINT pk_estado_expediente      PRIMARY KEY (id),
    CONSTRAINT uq_estado_expediente_codigo UNIQUE (codigo)
);
```

**`tipo_tramite`** — Tipos de trámite posibles
```sql
CREATE TABLE tipo_tramite (
    id             INTEGER      NOT NULL,
    codigo         VARCHAR(40)  NOT NULL,          -- REGISTRO | ASIGNACION | REVISION | PROPUESTA | RESOLUCION | NOTIFICACION
    descripcion_es VARCHAR(255) NOT NULL,
    descripcion_eu VARCHAR(255) NOT NULL,
    CONSTRAINT pk_tipo_tramite      PRIMARY KEY (id),
    CONSTRAINT uq_tipo_tramite_codigo UNIQUE (codigo)
);
```

**`usuario`** — Usuarios internos del sistema
```sql
CREATE TABLE usuario (
    id               BIGINT       NOT NULL GENERATED ALWAYS AS IDENTITY,
    username         VARCHAR(60)  NOT NULL,
    password_hash    VARCHAR(255) NOT NULL,         -- BCrypt, nunca en claro
    nombre           VARCHAR(100) NOT NULL,
    apellidos        VARCHAR(150) NOT NULL,
    email            VARCHAR(150),
    idioma_preferido VARCHAR(5)   NOT NULL DEFAULT 'es',  -- es | eu
    activo           BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_alta       TIMESTAMP    NOT NULL,
    CONSTRAINT pk_usuario          PRIMARY KEY (id),
    CONSTRAINT uq_usuario_username UNIQUE (username),
    CONSTRAINT ck_usuario_idioma   CHECK (idioma_preferido IN ('es', 'eu'))
);
```

**`rol`** y **`usuario_rol`** — Roles y asignaciones
```sql
CREATE TABLE rol (
    id             INTEGER      NOT NULL,
    codigo         VARCHAR(40)  NOT NULL,          -- CONSULTOR | INSTRUCTOR | RESPONSABLE | ADMIN
    descripcion_es VARCHAR(255) NOT NULL,
    descripcion_eu VARCHAR(255) NOT NULL,
    CONSTRAINT pk_rol      PRIMARY KEY (id),
    CONSTRAINT uq_rol_codigo UNIQUE (codigo)
);

CREATE TABLE usuario_rol (
    usuario_id BIGINT  NOT NULL,
    rol_id     INTEGER NOT NULL,
    CONSTRAINT pk_usuario_rol PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT fk_ur_usuario  FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT fk_ur_rol      FOREIGN KEY (rol_id)     REFERENCES rol (id)
);
```

---

### 2. Personas

**`persona`** — Personas físicas (solicitantes, representantes, causantes)
```sql
CREATE TABLE persona (
    id               BIGINT       NOT NULL GENERATED ALWAYS AS IDENTITY,
    tipo_documento   VARCHAR(10)  NOT NULL,         -- DNI | NIE | PAS
    numero_documento VARCHAR(20)  NOT NULL,
    nombre           VARCHAR(100) NOT NULL,
    apellido1        VARCHAR(100) NOT NULL,
    apellido2        VARCHAR(100),
    fecha_nacimiento DATE,
    sexo             VARCHAR(1),
    CONSTRAINT pk_persona    PRIMARY KEY (id),
    CONSTRAINT uq_persona_doc UNIQUE (tipo_documento, numero_documento)
);
```

---

### 3. Núcleo del expediente

**`expediente`** — Entidad central
```sql
CREATE TABLE expediente (
    id                BIGINT      NOT NULL GENERATED ALWAYS AS IDENTITY,
    numero_expediente VARCHAR(30) NOT NULL,
    tipo_ayuda_id     INTEGER     NOT NULL,
    estado_id         INTEGER     NOT NULL,
    fecha_registro    TIMESTAMP   NOT NULL,
    instructor_id     BIGINT,                       -- FK usuario con rol INSTRUCTOR (nullable)
    fecha_resolucion  DATE,
    CONSTRAINT pk_expediente        PRIMARY KEY (id),
    CONSTRAINT uq_expediente_numero UNIQUE (numero_expediente),
    CONSTRAINT fk_exp_tipo          FOREIGN KEY (tipo_ayuda_id) REFERENCES tipo_ayuda (id),
    CONSTRAINT fk_exp_estado        FOREIGN KEY (estado_id)     REFERENCES estado_expediente (id),
    CONSTRAINT fk_exp_instructor    FOREIGN KEY (instructor_id) REFERENCES usuario (id)
);
```

**`expediente_interesado`** — Vincula personas al expediente con su rol
```sql
CREATE TABLE expediente_interesado (
    id             BIGINT      NOT NULL GENERATED ALWAYS AS IDENTITY,
    expediente_id  BIGINT      NOT NULL,
    persona_id     BIGINT      NOT NULL,
    rol_interesado VARCHAR(20) NOT NULL,            -- SOLICITANTE | REPRESENTANTE | CAUSANTE
    CONSTRAINT pk_exp_int    PRIMARY KEY (id),
    CONSTRAINT fk_ei_exp     FOREIGN KEY (expediente_id) REFERENCES expediente (id),
    CONSTRAINT fk_ei_persona FOREIGN KEY (persona_id)    REFERENCES persona (id),
    CONSTRAINT uq_ei UNIQUE (expediente_id, persona_id, rol_interesado)
);
```

**`datos_contacto`** — Sección de contacto y aviso (1:1 con expediente)
```sql
CREATE TABLE datos_contacto (
    id                      BIGINT     NOT NULL GENERATED ALWAYS AS IDENTITY,
    expediente_id           BIGINT     NOT NULL,
    via                     VARCHAR(150),
    numero                  VARCHAR(10),
    piso                    VARCHAR(10),
    codigo_postal           VARCHAR(10),
    municipio               VARCHAR(100),
    provincia               VARCHAR(100),
    telefono                VARCHAR(20),
    email                   VARCHAR(150),
    canal_aviso             VARCHAR(20),            -- EMAIL | SMS | POSTAL
    idioma_notificacion     VARCHAR(5),             -- es | eu (independiente del idioma de interfaz)
    acepta_notif_telematica BOOLEAN    NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_datos_contacto PRIMARY KEY (id),
    CONSTRAINT fk_dc_exp         FOREIGN KEY (expediente_id) REFERENCES expediente (id),
    CONSTRAINT uq_dc_exp         UNIQUE (expediente_id),
    CONSTRAINT ck_dc_idioma      CHECK (idioma_notificacion IN ('es', 'eu'))
);
```

**`solicitud_ayuda`** — Datos comunes de la solicitud (1:1 con expediente)
```sql
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
```

---

### 4. Detalle por tipo de ayuda

**`detalle_asignacion_discapacidad`** — Ayuda 1 (ASIG_DISCAP)
```sql
CREATE TABLE detalle_asignacion_discapacidad (
    solicitud_id             BIGINT  NOT NULL,
    causante_persona_id      BIGINT  NOT NULL,
    grado_discapacidad       INTEGER NOT NULL,       -- porcentaje 0–100
    necesita_tercera_persona BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_det_asig  PRIMARY KEY (solicitud_id, causante_persona_id),
    CONSTRAINT fk_da_sol    FOREIGN KEY (solicitud_id)        REFERENCES solicitud_ayuda (id),
    CONSTRAINT fk_da_persona FOREIGN KEY (causante_persona_id) REFERENCES persona (id),
    CONSTRAINT ck_da_grado  CHECK (grado_discapacidad BETWEEN 0 AND 100)
);
```

**`detalle_nacimiento_adopcion`** — Ayuda 2 (PAGO_NAC)
```sql
CREATE TABLE detalle_nacimiento_adopcion (
    solicitud_id       BIGINT        NOT NULL,
    supuesto           VARCHAR(20)   NOT NULL,       -- FAMILIA_NUMEROSA | MONOPARENTAL | DISCAPACIDAD
    fecha_hecho        DATE          NOT NULL,
    ingresos_anuales   DECIMAL(12,2),
    numero_hijos_cargo INTEGER,
    CONSTRAINT pk_det_nac PRIMARY KEY (solicitud_id),
    CONSTRAINT fk_dna_sol  FOREIGN KEY (solicitud_id) REFERENCES solicitud_ayuda (id)
);
```

**`detalle_parto_multiple`** — Ayuda 3 (PARTO_MULT)
```sql
CREATE TABLE detalle_parto_multiple (
    solicitud_id   BIGINT  NOT NULL,
    numero_nacidos INTEGER NOT NULL,                  -- mínimo 2 → cuantía: 4/8/12 × SMI
    fecha_hecho    DATE    NOT NULL,
    CONSTRAINT pk_det_parto PRIMARY KEY (solicitud_id),
    CONSTRAINT fk_dpm_sol   FOREIGN KEY (solicitud_id) REFERENCES solicitud_ayuda (id),
    CONSTRAINT ck_dpm_num   CHECK (numero_nacidos >= 2)
);
```

---

### 5. Tramitación (Fase 1)

**`tramite`** — Registro histórico de trámites
```sql
CREATE TABLE tramite (
    id              BIGINT       NOT NULL GENERATED ALWAYS AS IDENTITY,
    expediente_id   BIGINT       NOT NULL,
    tipo_tramite_id INTEGER      NOT NULL,
    fecha           TIMESTAMP    NOT NULL,
    usuario_id      BIGINT       NOT NULL,            -- quién realizó el trámite
    resultado       VARCHAR(30),                      -- FAVORABLE | DESFAVORABLE
    observaciones   VARCHAR(1000),
    CONSTRAINT pk_tramite    PRIMARY KEY (id),
    CONSTRAINT fk_tr_exp     FOREIGN KEY (expediente_id)   REFERENCES expediente (id),
    CONSTRAINT fk_tr_tipo    FOREIGN KEY (tipo_tramite_id) REFERENCES tipo_tramite (id),
    CONSTRAINT fk_tr_usuario FOREIGN KEY (usuario_id)      REFERENCES usuario (id)
);
```

---

### 6. Auditoría

**`auditoria`** — Trazabilidad de todas las operaciones relevantes
```sql
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
```

---

## Índices

```sql
CREATE INDEX idx_expediente_estado  ON expediente (estado_id);
CREATE INDEX idx_expediente_tipo    ON expediente (tipo_ayuda_id);
CREATE INDEX idx_expediente_fecha   ON expediente (fecha_registro);
CREATE INDEX idx_persona_documento  ON persona (numero_documento);
CREATE INDEX idx_tramite_expediente ON tramite (expediente_id);
CREATE INDEX idx_tramite_fecha      ON tramite (fecha);
CREATE INDEX idx_auditoria_fecha    ON auditoria (fecha_hora);
CREATE INDEX idx_auditoria_usuario  ON auditoria (usuario_id);
```

---

## Notas de diseño

- **Bilingüismo**: las tablas de catálogo guardan `descripcion_es` y `descripcion_eu`. El código (`codigo`) es el identificador estable independiente del idioma.
- **Datos capturados**: nombres, domicilios, observaciones se almacenan tal cual (sin traducir).
- **Idioma de notificación**: `datos_contacto.idioma_notificacion` es independiente de `usuario.idioma_preferido`.
- **Seguridad de contraseñas**: `password_hash` almacena siempre el hash BCrypt, nunca la contraseña en claro.
- **Paginación**: implementada en SQL (`LIMIT` / `OFFSET`) con índices en columnas de filtro frecuente.
