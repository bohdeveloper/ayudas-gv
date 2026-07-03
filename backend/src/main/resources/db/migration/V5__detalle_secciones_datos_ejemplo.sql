-- ============================================================
-- V5: Completar datos de ejemplo para las secciones de detalle
-- Semana 5 — Fase 0
--
-- V2 dejó interesados/contacto/solicitud incompletos en varios expedientes
-- (EXP-2024-003/004/005 sin interesados ni contacto; EXP-2024-002 sin
-- detalle de su solicitud). Para poder navegar las 4 secciones de detalle
-- de un expediente de cada tipo de ayuda, se completan aquí.
-- ============================================================

-- EXP-2024-002 (PAGO_NAC): añade el detalle que le faltaba a su solicitud
INSERT INTO detalle_nacimiento_adopcion (solicitud_id, supuesto, fecha_hecho, ingresos_anuales, numero_hijos_cargo)
    SELECT sa.id, 'FAMILIA_NUMEROSA', '2024-01-05', 28500.00, 3
    FROM solicitud_ayuda sa
    JOIN expediente e ON sa.expediente_id = e.id
    WHERE e.numero_expediente = 'EXP-2024-002';

-- EXP-2024-003 (PARTO_MULT): le faltaban interesado y contacto
INSERT INTO expediente_interesado (expediente_id, persona_id, rol_interesado)
    SELECT e.id, p.id, 'SOLICITANTE'
    FROM expediente e, persona p
    WHERE e.numero_expediente = 'EXP-2024-003' AND p.numero_documento = 'X1234567Z';

INSERT INTO datos_contacto (expediente_id, via, numero, codigo_postal, municipio, provincia, telefono, email, canal_aviso, idioma_notificacion, acepta_notif_telematica)
    SELECT id, 'Avenida Libertad', '30', '01001', 'Vitoria-Gasteiz', 'Álava', '600555666', 'ana.fernandez@email.com', 'SMS', 'es', FALSE
    FROM expediente WHERE numero_expediente = 'EXP-2024-003';

-- EXP-2024-004 (ASIG_DISCAP): expediente sin ninguna sección — se completa
-- con solicitante + representante (demuestra el campo opcional) + causante,
-- y una discapacidad ≥75% con necesidad de tercera persona (caso especial de Fase 4)
INSERT INTO expediente_interesado (expediente_id, persona_id, rol_interesado)
    SELECT e.id, p.id, 'SOLICITANTE'
    FROM expediente e, persona p
    WHERE e.numero_expediente = 'EXP-2024-004' AND p.numero_documento = '12345678A';

INSERT INTO expediente_interesado (expediente_id, persona_id, rol_interesado)
    SELECT e.id, p.id, 'REPRESENTANTE'
    FROM expediente e, persona p
    WHERE e.numero_expediente = 'EXP-2024-004' AND p.numero_documento = 'X1234567Z';

INSERT INTO expediente_interesado (expediente_id, persona_id, rol_interesado)
    SELECT e.id, p.id, 'CAUSANTE'
    FROM expediente e, persona p
    WHERE e.numero_expediente = 'EXP-2024-004' AND p.numero_documento = '44332211D';

INSERT INTO datos_contacto (expediente_id, via, numero, piso, codigo_postal, municipio, provincia, telefono, email, canal_aviso, idioma_notificacion, acepta_notif_telematica)
    SELECT id, 'Calle Mayor', '5', '2ºB', '20100', 'Errenteria', 'Gipuzkoa', '600111222', 'maria.garcia@email.com', 'POSTAL', 'es', FALSE
    FROM expediente WHERE numero_expediente = 'EXP-2024-004';

INSERT INTO solicitud_ayuda (expediente_id, fecha_solicitud, importe_solicitado, observaciones)
    SELECT id, '2024-02-01', NULL, 'Pendiente de asignar instructor'
    FROM expediente WHERE numero_expediente = 'EXP-2024-004';

INSERT INTO detalle_asignacion_discapacidad (solicitud_id, causante_persona_id, grado_discapacidad, necesita_tercera_persona)
    SELECT sa.id, p.id, 78, TRUE
    FROM solicitud_ayuda sa
    JOIN expediente e ON sa.expediente_id = e.id
    JOIN persona p ON p.numero_documento = '44332211D'
    WHERE e.numero_expediente = 'EXP-2024-004';

-- EXP-2024-005 (PAGO_NAC): expediente sin ninguna sección — se completa
-- con un supuesto distinto al de EXP-2024-002 (MONOPARENTAL)
INSERT INTO expediente_interesado (expediente_id, persona_id, rol_interesado)
    SELECT e.id, p.id, 'SOLICITANTE'
    FROM expediente e, persona p
    WHERE e.numero_expediente = 'EXP-2024-005' AND p.numero_documento = '87654321B';

INSERT INTO datos_contacto (expediente_id, via, numero, codigo_postal, municipio, provincia, telefono, email, canal_aviso, idioma_notificacion, acepta_notif_telematica)
    SELECT id, 'Kale Nagusia', '12', '20400', 'Tolosa', 'Gipuzkoa', '600333444', 'jon.etxeberria@email.com', 'EMAIL', 'eu', TRUE
    FROM expediente WHERE numero_expediente = 'EXP-2024-005';

INSERT INTO solicitud_ayuda (expediente_id, fecha_solicitud, importe_solicitado)
    SELECT id, '2024-02-10', 350.00
    FROM expediente WHERE numero_expediente = 'EXP-2024-005';

INSERT INTO detalle_nacimiento_adopcion (solicitud_id, supuesto, fecha_hecho, ingresos_anuales, numero_hijos_cargo)
    SELECT sa.id, 'MONOPARENTAL', '2024-01-28', 19800.00, 2
    FROM solicitud_ayuda sa
    JOIN expediente e ON sa.expediente_id = e.id
    WHERE e.numero_expediente = 'EXP-2024-005';
