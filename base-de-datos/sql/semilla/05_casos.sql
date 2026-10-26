-- DATOS SIMULADOS generados por scripts/generar_semilla.py (no editar a mano).
-- Ninguna persona es real. Ver documentacion/datos_simulados.md

INSERT INTO casos (id_caso, id_incidente, id_estado_caso, id_operador, fecha_creacion, fecha_cierre, observacion, fecha_consentimiento) VALUES
  (1, 2, 6, 2, '2026-06-10T12:06:43-05:00', '2026-07-21T14:06:43-05:00', 'Incidente sin antecedentes registrados.', '2026-06-10T12:06:43-05:00'),
  (2, 3, 4, 4, '2026-06-11T01:45:19-05:00', NULL, 'Incidente sin antecedentes registrados.', '2026-06-11T01:45:19-05:00'),
  (3, 5, 5, 2, '2026-06-20T22:05:14-05:00', '2026-07-20T22:05:14-05:00', 'Incidente sin antecedentes registrados.', '2026-06-20T22:05:14-05:00'),
  (4, 7, 3, 4, '2026-07-22T19:40:00-05:00', NULL, 'Incidente con antecedentes previos en el punto.', '2026-07-22T19:40:00-05:00'),
  (5, 10, 4, 3, '2026-08-12T12:50:00-05:00', NULL, 'Incidente con antecedentes previos en el punto.', '2026-08-12T12:50:00-05:00'),
  (6, 11, 6, 4, '2026-08-26T10:15:00-05:00', '2026-09-16T06:15:00-05:00', 'Incidente con antecedentes previos en el punto.', '2026-08-26T10:15:00-05:00'),
  (7, 13, 3, 3, '2026-08-29T06:05:00-05:00', NULL, 'Incidente con antecedentes previos en el punto.', '2026-08-29T06:05:00-05:00'),
  (8, 15, 2, 3, '2026-10-03T10:00:00-05:00', NULL, 'Incidente con antecedentes previos en el punto.', '2026-10-03T10:00:00-05:00');

INSERT INTO postulaciones (id_postulacion, id_caso, id_abogado, mensaje, estado, fecha_postulacion, fecha_resolucion) VALUES
  (1, 1, 2, 'Tengo experiencia en demandas de reparación directa contra el municipio por vías en mal estado. Con los antecedentes del punto el caso es sólido; puedo revisarlo esta misma semana.', 'ELEGIDA', '2026-06-11T05:06:43-05:00', '2026-06-14T14:06:43-05:00'),
  (2, 2, 4, 'Tengo experiencia en demandas de reparación directa contra el municipio por vías en mal estado. Con los antecedentes del punto el caso es sólido; puedo revisarlo esta misma semana.', 'NO_ELEGIDA', '2026-06-11T07:45:19-05:00', '2026-06-13T13:45:19-05:00'),
  (3, 2, 2, 'He llevado varios casos de falla en el servicio por alcantarillado y alumbrado. Le explico el proceso sin costo inicial y reviso con usted la evidencia y los plazos.', 'ELEGIDA', '2026-06-11T14:45:19-05:00', '2026-06-13T13:45:19-05:00'),
  (4, 1, 3, 'He llevado varios casos de falla en el servicio por alcantarillado y alumbrado. Le explico el proceso sin costo inicial y reviso con usted la evidencia y los plazos.', 'NO_ELEGIDA', '2026-06-11T15:06:43-05:00', '2026-06-14T14:06:43-05:00'),
  (5, 1, 1, 'Trabajo responsabilidad del Estado hace años. Los reportes previos en el mismo sitio ayudan mucho a demostrar que la entidad conocía la falla. Me interesa acompañarlo.', 'NO_ELEGIDA', '2026-06-12T15:06:43-05:00', '2026-06-14T14:06:43-05:00'),
  (6, 4, 1, 'Tengo experiencia en demandas de reparación directa contra el municipio por vías en mal estado. Con los antecedentes del punto el caso es sólido; puedo revisarlo esta misma semana.', 'PENDIENTE', '2026-07-23T12:40:00-05:00', NULL),
  (7, 4, 2, 'He llevado varios casos de falla en el servicio por alcantarillado y alumbrado. Le explico el proceso sin costo inicial y reviso con usted la evidencia y los plazos.', 'PENDIENTE', '2026-07-24T03:40:00-05:00', NULL),
  (8, 4, 3, 'Trabajo responsabilidad del Estado hace años. Los reportes previos en el mismo sitio ayudan mucho a demostrar que la entidad conocía la falla. Me interesa acompañarlo.', 'PENDIENTE', '2026-07-24T16:40:00-05:00', NULL),
  (9, 5, 1, 'Tengo experiencia en demandas de reparación directa contra el municipio por vías en mal estado. Con los antecedentes del punto el caso es sólido; puedo revisarlo esta misma semana.', 'NO_ELEGIDA', '2026-08-12T20:50:00-05:00', '2026-08-15T04:50:00-05:00'),
  (10, 5, 4, 'He llevado varios casos de falla en el servicio por alcantarillado y alumbrado. Le explico el proceso sin costo inicial y reviso con usted la evidencia y los plazos.', 'ELEGIDA', '2026-08-13T04:50:00-05:00', '2026-08-15T04:50:00-05:00'),
  (11, 5, 2, 'Trabajo responsabilidad del Estado hace años. Los reportes previos en el mismo sitio ayudan mucho a demostrar que la entidad conocía la falla. Me interesa acompañarlo.', 'NO_ELEGIDA', '2026-08-14T04:50:00-05:00', '2026-08-15T04:50:00-05:00'),
  (12, 6, 2, 'Tengo experiencia en demandas de reparación directa contra el municipio por vías en mal estado. Con los antecedentes del punto el caso es sólido; puedo revisarlo esta misma semana.', 'ELEGIDA', '2026-08-27T03:15:00-05:00', '2026-08-30T06:15:00-05:00'),
  (13, 6, 3, 'He llevado varios casos de falla en el servicio por alcantarillado y alumbrado. Le explico el proceso sin costo inicial y reviso con usted la evidencia y los plazos.', 'NO_ELEGIDA', '2026-08-27T22:15:00-05:00', '2026-08-30T06:15:00-05:00'),
  (14, 6, 1, 'Trabajo responsabilidad del Estado hace años. Los reportes previos en el mismo sitio ayudan mucho a demostrar que la entidad conocía la falla. Me interesa acompañarlo.', 'NO_ELEGIDA', '2026-08-28T21:15:00-05:00', '2026-08-30T06:15:00-05:00'),
  (15, 7, 2, 'Tengo experiencia en demandas de reparación directa contra el municipio por vías en mal estado. Con los antecedentes del punto el caso es sólido; puedo revisarlo esta misma semana.', 'PENDIENTE', '2026-08-30T03:05:00-05:00', NULL),
  (16, 7, 4, 'He llevado varios casos de falla en el servicio por alcantarillado y alumbrado. Le explico el proceso sin costo inicial y reviso con usted la evidencia y los plazos.', 'PENDIENTE', '2026-08-30T12:05:00-05:00', NULL),
  (17, 7, 1, 'Trabajo responsabilidad del Estado hace años. Los reportes previos en el mismo sitio ayudan mucho a demostrar que la entidad conocía la falla. Me interesa acompañarlo.', 'PENDIENTE', '2026-08-31T16:05:00-05:00', NULL);

INSERT INTO remisiones (id_remision, id_caso, id_abogado, fecha_envio, estado_remision, fecha_respuesta, motivo_rechazo, valor_cobrado) VALUES
  (1, 2, 2, '2026-06-13T13:45:19-05:00', 'ACEPTADA', '2026-06-13T13:45:19-05:00', NULL, 200000),
  (2, 1, 2, '2026-06-14T14:06:43-05:00', 'ACEPTADA', '2026-06-14T14:06:43-05:00', NULL, 200000),
  (3, 5, 4, '2026-08-15T04:50:00-05:00', 'ACEPTADA', '2026-08-15T04:50:00-05:00', NULL, 200000),
  (4, 6, 2, '2026-08-30T06:15:00-05:00', 'ACEPTADA', '2026-08-30T06:15:00-05:00', NULL, 200000);

