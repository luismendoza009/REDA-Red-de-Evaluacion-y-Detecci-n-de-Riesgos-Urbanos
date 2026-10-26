-- DATOS SIMULADOS generados por scripts/generar_semilla.py (no editar a mano).
-- Ninguna persona es real. Ver documentacion/datos_simulados.md

INSERT INTO casos (id_caso, id_incidente, id_estado_caso, id_operador, fecha_creacion, fecha_cierre, observacion) VALUES
  (1, 1, 4, 3, '2026-05-16T21:21:00-05:00', NULL, 'Incidente sin antecedentes registrados.'),
  (2, 2, 4, 2, '2026-05-16T06:10:57-05:00', NULL, 'Incidente sin antecedentes registrados.'),
  (3, 3, 4, 3, '2026-06-19T08:38:20-05:00', NULL, 'Incidente sin antecedentes registrados.'),
  (4, 5, 5, 3, '2026-07-27T19:00:38-05:00', NULL, 'Incidente sin antecedentes registrados.'),
  (5, 6, 5, 3, '2026-07-30T01:40:00-05:00', NULL, 'Incidente con antecedentes previos en el punto.'),
  (6, 7, 4, 2, '2026-08-14T00:46:19-05:00', NULL, 'Incidente sin antecedentes registrados.'),
  (7, 8, 5, 2, '2026-08-25T12:50:00-05:00', NULL, 'Incidente con antecedentes previos en el punto.'),
  (8, 11, 6, 3, '2026-08-31T01:10:00-05:00', '2026-09-25T20:10:00-05:00', 'Incidente con antecedentes previos en el punto.'),
  (9, 13, 4, 3, '2026-08-29T04:05:00-05:00', NULL, 'Incidente con antecedentes previos en el punto.'),
  (10, 15, 4, 4, '2026-10-03T04:00:00-05:00', NULL, 'Incidente con antecedentes previos en el punto.');

INSERT INTO remisiones (id_remision, id_caso, id_abogado, fecha_envio, estado_remision, fecha_respuesta, motivo_rechazo, valor_cobrado) VALUES
  (1, 2, 4, '2026-05-17T06:10:57-05:00', 'ACEPTADA', '2026-05-19T06:10:57-05:00', NULL, 200000),
  (2, 1, 1, '2026-05-17T13:21:00-05:00', 'ACEPTADA', '2026-05-20T05:21:00-05:00', NULL, 200000),
  (3, 3, 1, '2026-06-20T01:38:20-05:00', 'RECHAZADA', '2026-06-21T02:38:20-05:00', 'Sin disponibilidad para nuevos casos.', 200000),
  (4, 3, 3, '2026-06-21T06:38:20-05:00', 'ACEPTADA', '2026-06-24T02:38:20-05:00', NULL, 200000),
  (5, 4, 4, '2026-07-28T05:00:38-05:00', 'RECHAZADA', '2026-07-29T12:00:38-05:00', 'Evidencia insuficiente del daño.', 120000),
  (6, 4, 1, '2026-07-29T14:00:38-05:00', 'RECHAZADA', '2026-07-30T10:00:38-05:00', 'Plazo de caducidad comprometido.', 120000),
  (7, 5, 2, '2026-07-30T06:40:00-05:00', 'RECHAZADA', '2026-08-02T01:40:00-05:00', 'Plazo de caducidad comprometido.', 200000),
  (8, 5, 3, '2026-08-02T03:40:00-05:00', 'RECHAZADA', '2026-08-04T15:40:00-05:00', 'Plazo de caducidad comprometido.', 200000),
  (9, 6, 1, '2026-08-14T10:46:19-05:00', 'ACEPTADA', '2026-08-15T11:46:19-05:00', NULL, 200000),
  (10, 7, 1, '2026-08-25T17:50:00-05:00', 'RECHAZADA', '2026-08-26T07:50:00-05:00', 'Plazo de caducidad comprometido.', 120000),
  (11, 7, 2, '2026-08-26T09:50:00-05:00', 'RECHAZADA', '2026-08-29T07:50:00-05:00', 'Plazo de caducidad comprometido.', 120000),
  (12, 9, 1, '2026-08-29T14:05:00-05:00', 'ACEPTADA', '2026-08-31T19:05:00-05:00', NULL, 120000),
  (13, 8, 2, '2026-08-31T20:10:00-05:00', 'ACEPTADA', '2026-09-04T20:10:00-05:00', NULL, 200000),
  (14, 10, 3, '2026-10-03T11:00:00-05:00', 'ACEPTADA', '2026-10-05T15:00:00-05:00', NULL, 120000);

