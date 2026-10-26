-- DATOS SIMULADOS generados por scripts/generar_semilla.py (no editar a mano).
-- Ninguna persona es real. Ver documentacion/datos_simulados.md

INSERT INTO casos (id_caso, id_incidente, id_estado_caso, id_operador, fecha_creacion, fecha_cierre, observacion) VALUES
  (1, 2, 6, 2, '2026-06-10T12:06:43-05:00', '2026-07-21T21:06:43-05:00', 'Incidente sin antecedentes registrados.'),
  (2, 3, 4, 3, '2026-06-11T23:45:19-05:00', NULL, 'Incidente sin antecedentes registrados.'),
  (3, 5, 5, 3, '2026-06-21T20:05:14-05:00', NULL, 'Incidente sin antecedentes registrados.'),
  (4, 7, 4, 3, '2026-07-22T11:40:00-05:00', NULL, 'Incidente con antecedentes previos en el punto.'),
  (5, 10, 3, 2, '2026-08-12T22:50:00-05:00', NULL, 'Incidente con antecedentes previos en el punto.'),
  (6, 11, 6, 3, '2026-08-24T21:15:00-05:00', '2026-09-19T16:15:00-05:00', 'Incidente con antecedentes previos en el punto.'),
  (7, 13, 4, 4, '2026-08-29T04:05:00-05:00', NULL, 'Incidente con antecedentes previos en el punto.'),
  (8, 15, 2, 2, '2026-10-02T12:00:00-05:00', NULL, 'Incidente con antecedentes previos en el punto.');

INSERT INTO remisiones (id_remision, id_caso, id_abogado, fecha_envio, estado_remision, fecha_respuesta, motivo_rechazo, valor_cobrado) VALUES
  (1, 1, 2, '2026-06-11T05:06:43-05:00', 'ACEPTADA', '2026-06-14T21:06:43-05:00', NULL, 200000),
  (2, 2, 1, '2026-06-12T09:45:19-05:00', 'RECHAZADA', '2026-06-13T05:45:19-05:00', 'Sin disponibilidad para nuevos casos.', 200000),
  (3, 2, 4, '2026-06-13T17:45:19-05:00', 'ACEPTADA', '2026-06-15T18:45:19-05:00', NULL, 200000),
  (4, 3, 2, '2026-06-22T01:05:14-05:00', 'RECHAZADA', '2026-06-24T20:05:14-05:00', 'Plazo de caducidad comprometido.', 200000),
  (5, 3, 3, '2026-06-24T22:05:14-05:00', 'RECHAZADA', '2026-06-27T10:05:14-05:00', 'Plazo de caducidad comprometido.', 200000),
  (6, 4, 4, '2026-07-23T10:40:00-05:00', 'RECHAZADA', '2026-07-25T11:40:00-05:00', 'Sin disponibilidad para nuevos casos.', 120000),
  (7, 4, 1, '2026-07-25T18:40:00-05:00', 'ACEPTADA', '2026-07-29T17:40:00-05:00', NULL, 120000),
  (8, 5, 1, '2026-08-12T23:50:00-05:00', 'ENVIADA', NULL, NULL, 200000),
  (9, 6, 2, '2026-08-25T16:15:00-05:00', 'ACEPTADA', '2026-08-29T16:15:00-05:00', NULL, 200000),
  (10, 7, 1, '2026-08-29T22:05:00-05:00', 'ACEPTADA', '2026-09-01T00:05:00-05:00', NULL, 120000);

