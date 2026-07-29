-- ============================================================
-- V6: Histórico de trámites de ejemplo
-- Fase 1 — Tramitación
--
-- V4 creó la tabla 'tramite' pero la dejó vacía. Sin trámites, la sección
-- Tramitación del detalle sale en blanco y no se puede demostrar el flujo.
-- Aquí se rellena el histórico de tres expedientes, coherente con el estado
-- que V2 les dejó:
--
--   EXP-2024-001 (ASIGNADO)    → REGISTRO + ASIGNACION
--   EXP-2024-003 (EN_REVISION) → REGISTRO + ASIGNACION + REVISION
--   EXP-2024-005 (RESUELTO)    → el flujo completo hasta NOTIFICACION
--
-- Los ids de expediente y usuario son autogenerados, así que se resuelven por
-- número de expediente y por username, que sí son estables (UNIQUE en V1/V2).
--
-- Añade además un segundo instructor: con uno solo, el desplegable de asignación
-- no tiene nada que elegir y la regla "un instructor no puede tramitar lo que no
-- es suyo" no se puede demostrar en la demo.
-- ============================================================

-- ── Segundo instructor (misma contraseña de prueba: admin123) ────────────────
INSERT INTO usuario (username, password_hash, nombre, apellidos, email, idioma_preferido, activo, fecha_alta) VALUES
    ('instructor2', '$2a$10$5QScUkS.9ejdgv1VHB12g.Mx47R09oTvZY7Ke51WPp6wc48ie8K6i',
                    'Miren', 'Agirre', 'magirre@ayudasgv.eus', 'eu', TRUE, NOW());

INSERT INTO usuario_rol (usuario_id, rol_id)
    SELECT u.id, r.id FROM usuario u, rol r WHERE u.username = 'instructor2' AND r.codigo = 'INSTRUCTOR';

-- ── EXP-2024-001 — registrado y asignado a instructor1, pendiente de revisar ──
INSERT INTO tramite (expediente_id, tipo_tramite_id, fecha, usuario_id, resultado, observaciones)
    SELECT e.id, tt.id, TIMESTAMP '2024-01-10 09:05:00', u.id, NULL,
           'Entrada por registro telemático'
    FROM expediente e, tipo_tramite tt, usuario u
    WHERE e.numero_expediente = 'EXP-2024-001' AND tt.codigo = 'REGISTRO' AND u.username = 'admin';

INSERT INTO tramite (expediente_id, tipo_tramite_id, fecha, usuario_id, resultado, observaciones)
    SELECT e.id, tt.id, TIMESTAMP '2024-01-11 12:30:00', u.id, NULL,
           'Asignado a Ane Etxeberria por carga de trabajo'
    FROM expediente e, tipo_tramite tt, usuario u
    WHERE e.numero_expediente = 'EXP-2024-001' AND tt.codigo = 'ASIGNACION' AND u.username = 'admin';

-- ── EXP-2024-003 — en revisión: se ha requerido subsanación al interesado ────
INSERT INTO tramite (expediente_id, tipo_tramite_id, fecha, usuario_id, resultado, observaciones)
    SELECT e.id, tt.id, TIMESTAMP '2024-01-20 11:05:00', u.id, NULL,
           'Entrada por registro presencial'
    FROM expediente e, tipo_tramite tt, usuario u
    WHERE e.numero_expediente = 'EXP-2024-003' AND tt.codigo = 'REGISTRO' AND u.username = 'admin';

INSERT INTO tramite (expediente_id, tipo_tramite_id, fecha, usuario_id, resultado, observaciones)
    SELECT e.id, tt.id, TIMESTAMP '2024-01-21 09:15:00', u.id, NULL, NULL
    FROM expediente e, tipo_tramite tt, usuario u
    WHERE e.numero_expediente = 'EXP-2024-003' AND tt.codigo = 'ASIGNACION' AND u.username = 'admin';

INSERT INTO tramite (expediente_id, tipo_tramite_id, fecha, usuario_id, resultado, observaciones)
    SELECT e.id, tt.id, TIMESTAMP '2024-01-25 10:40:00', u.id, NULL,
           'Falta el certificado de convivencia: se requiere subsanación en 10 días hábiles'
    FROM expediente e, tipo_tramite tt, usuario u
    WHERE e.numero_expediente = 'EXP-2024-003' AND tt.codigo = 'REVISION' AND u.username = 'instructor1';

-- ── EXP-2024-005 — flujo completo: registro → asignación → revisión →
--    propuesta favorable → resolución → notificación al interesado ──────────
INSERT INTO tramite (expediente_id, tipo_tramite_id, fecha, usuario_id, resultado, observaciones)
    SELECT e.id, tt.id, TIMESTAMP '2024-02-10 14:05:00', u.id, NULL, NULL
    FROM expediente e, tipo_tramite tt, usuario u
    WHERE e.numero_expediente = 'EXP-2024-005' AND tt.codigo = 'REGISTRO' AND u.username = 'admin';

INSERT INTO tramite (expediente_id, tipo_tramite_id, fecha, usuario_id, resultado, observaciones)
    SELECT e.id, tt.id, TIMESTAMP '2024-02-11 08:20:00', u.id, NULL, NULL
    FROM expediente e, tipo_tramite tt, usuario u
    WHERE e.numero_expediente = 'EXP-2024-005' AND tt.codigo = 'ASIGNACION' AND u.username = 'admin';

INSERT INTO tramite (expediente_id, tipo_tramite_id, fecha, usuario_id, resultado, observaciones)
    SELECT e.id, tt.id, TIMESTAMP '2024-02-14 11:00:00', u.id, NULL,
           'Documentación completa y verificada'
    FROM expediente e, tipo_tramite tt, usuario u
    WHERE e.numero_expediente = 'EXP-2024-005' AND tt.codigo = 'REVISION' AND u.username = 'instructor1';

INSERT INTO tramite (expediente_id, tipo_tramite_id, fecha, usuario_id, resultado, observaciones)
    SELECT e.id, tt.id, TIMESTAMP '2024-02-18 09:45:00', u.id, 'FAVORABLE',
           'Cumple los requisitos: se propone la concesión'
    FROM expediente e, tipo_tramite tt, usuario u
    WHERE e.numero_expediente = 'EXP-2024-005' AND tt.codigo = 'PROPUESTA' AND u.username = 'instructor1';

INSERT INTO tramite (expediente_id, tipo_tramite_id, fecha, usuario_id, resultado, observaciones)
    SELECT e.id, tt.id, TIMESTAMP '2024-02-20 13:10:00', u.id, 'FAVORABLE',
           'Resolución de concesión firmada'
    FROM expediente e, tipo_tramite tt, usuario u
    WHERE e.numero_expediente = 'EXP-2024-005' AND tt.codigo = 'RESOLUCION' AND u.username = 'admin';

INSERT INTO tramite (expediente_id, tipo_tramite_id, fecha, usuario_id, resultado, observaciones)
    SELECT e.id, tt.id, TIMESTAMP '2024-02-21 10:00:00', u.id, NULL,
           'Notificada por correo electrónico en castellano (canal e idioma de datos_contacto)'
    FROM expediente e, tipo_tramite tt, usuario u
    WHERE e.numero_expediente = 'EXP-2024-005' AND tt.codigo = 'NOTIFICACION' AND u.username = 'admin';

-- El expediente resuelto debe llevar su fecha de resolución: V2 lo dejó en RESUELTO
-- pero con fecha_resolucion a NULL, lo que era incoherente con su propio estado.
UPDATE expediente
SET    fecha_resolucion = DATE '2024-02-20'
WHERE  numero_expediente = 'EXP-2024-005' AND fecha_resolucion IS NULL;
