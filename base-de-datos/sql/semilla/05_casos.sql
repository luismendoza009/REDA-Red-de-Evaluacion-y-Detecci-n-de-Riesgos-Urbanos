-- DATOS SIMULADOS generados por scripts/generar_semilla.py (no editar a mano).
-- Ninguna persona es real. Ver documentacion/datos_simulados.md

INSERT INTO casos (id_caso, id_incidente, id_estado_caso, id_operador, fecha_creacion, fecha_cierre, observacion) VALUES
  (1, 2, 4, 4, '2026-06-10T12:06:43-05:00', NULL, 'Incidente sin antecedentes registrados.'),
  (2, 3, 4, 4, '2026-06-11T01:45:19-05:00', NULL, 'Incidente sin antecedentes registrados.'),
  (3, 5, 3, 4, '2026-06-20T03:05:14-05:00', NULL, 'Incidente sin antecedentes registrados.'),
  (4, 6, 4, 4, '2026-07-20T05:59:59-05:00', NULL, 'Incidente sin antecedentes registrados.'),
  (5, 7, 3, 2, '2026-07-21T22:40:00-05:00', NULL, 'Incidente con antecedentes previos en el punto.'),
  (6, 10, 4, 2, '2026-08-12T22:50:00-05:00', NULL, 'Incidente con antecedentes previos en el punto.'),
  (7, 11, 4, 3, '2026-08-26T02:15:00-05:00', NULL, 'Incidente con antecedentes previos en el punto.'),
  (8, 13, 4, 3, '2026-08-29T16:05:00-05:00', NULL, 'Incidente con antecedentes previos en el punto.'),
  (9, 15, 4, 3, '2026-10-02T12:00:00-05:00', NULL, 'Incidente con antecedentes previos en el punto.');

INSERT INTO remisiones (id_remision, id_caso, id_abogado, fecha_envio, estado_remision, fecha_respuesta, motivo_rechazo, valor_cobrado) VALUES
  (1, 1, 3, '2026-06-10T20:06:43-05:00', 'ACEPTADA', '2026-06-12T00:06:43-05:00', NULL, 200000),
  (2, 2, 1, '2026-06-11T05:45:19-05:00', 'RECHAZADA', '2026-06-13T06:45:19-05:00', 'Sin disponibilidad para nuevos casos.', 200000),
  (3, 2, 3, '2026-06-13T12:45:19-05:00', 'ACEPTADA', '2026-06-17T07:45:19-05:00', NULL, 200000),
  (4, 3, 3, '2026-06-20T19:05:14-05:00', 'ENVIADA', NULL, NULL, 200000),
  (5, 4, 1, '2026-07-21T04:59:59-05:00', 'ACEPTADA', '2026-07-23T18:59:59-05:00', NULL, 120000),
  (6, 5, 1, '2026-07-22T03:40:00-05:00', 'ENVIADA', NULL, NULL, 120000),
  (7, 6, 2, '2026-08-13T03:50:00-05:00', 'ACEPTADA', '2026-08-13T14:50:00-05:00', NULL, 200000),
  (8, 7, 3, '2026-08-26T17:15:00-05:00', 'ACEPTADA', '2026-08-29T17:15:00-05:00', NULL, 200000),
  (9, 8, 1, '2026-08-29T18:05:00-05:00', 'RECHAZADA', '2026-08-31T18:05:00-05:00', 'Sin disponibilidad para nuevos casos.', 120000),
  (10, 8, 3, '2026-08-31T22:05:00-05:00', 'ACEPTADA', '2026-09-04T04:05:00-05:00', NULL, 120000),
  (11, 9, 4, '2026-10-03T07:00:00-05:00', 'RECHAZADA', '2026-10-03T19:00:00-05:00', 'Sin disponibilidad para nuevos casos.', 120000),
  (12, 9, 2, '2026-10-04T07:00:00-05:00', 'ACEPTADA', '2026-10-05T09:00:00-05:00', NULL, 120000);

