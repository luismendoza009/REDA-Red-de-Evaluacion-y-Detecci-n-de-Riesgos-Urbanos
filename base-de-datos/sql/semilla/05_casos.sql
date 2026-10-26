-- DATOS SIMULADOS generados por scripts/generar_semilla.py (no editar a mano).
-- Ninguna persona es real. Ver documentacion/datos_simulados.md

INSERT INTO casos (id_caso, id_incidente, id_estado_caso, id_operador, fecha_creacion, fecha_cierre, observacion, fecha_consentimiento) VALUES
  (1, 2, 6, 2, '2026-06-10T08:06:43-05:00', '2026-07-19T00:06:43-05:00', 'Incidente con antecedentes previos en el punto.', '2026-06-10T08:06:43-05:00'),
  (2, 3, 4, 4, '2026-06-10T10:45:19-05:00', NULL, 'Incidente sin antecedentes registrados.', '2026-06-10T10:45:19-05:00'),
  (3, 5, 6, 2, '2026-06-20T22:05:14-05:00', '2026-08-01T12:05:14-05:00', 'Incidente sin antecedentes registrados.', '2026-06-20T22:05:14-05:00'),
  (4, 7, 3, 4, '2026-07-22T16:40:00-05:00', NULL, 'Incidente con antecedentes previos en el punto.', '2026-07-22T16:40:00-05:00'),
  (5, 9, 6, 4, '2026-08-04T11:54:15-05:00', '2026-09-05T06:54:15-05:00', 'Incidente sin antecedentes registrados.', '2026-08-04T11:54:15-05:00'),
  (6, 10, 4, 3, '2026-08-13T05:50:00-05:00', NULL, 'Incidente con antecedentes previos en el punto.', '2026-08-13T05:50:00-05:00'),
  (7, 11, 3, 4, '2026-08-26T03:15:00-05:00', NULL, 'Incidente con antecedentes previos en el punto.', '2026-08-26T03:15:00-05:00'),
  (8, 13, 5, 3, '2026-08-30T18:05:00-05:00', '2026-09-29T18:05:00-05:00', 'Incidente con antecedentes previos en el punto.', '2026-08-30T18:05:00-05:00'),
  (9, 15, 2, 3, '2026-10-01T17:00:00-05:00', NULL, 'Incidente con antecedentes previos en el punto.', '2026-10-01T17:00:00-05:00');

INSERT INTO postulaciones (id_postulacion, id_caso, id_abogado, mensaje, estado, fecha_postulacion, fecha_resolucion) VALUES
  (1, 1, 1, 'Tengo experiencia en demandas de reparación directa contra el municipio por vías en mal estado. Con los antecedentes del punto el caso es sólido; puedo revisarlo esta misma semana.', 'ELEGIDA', '2026-06-10T18:06:43-05:00', '2026-06-12T00:06:43-05:00'),
  (2, 1, 4, 'He llevado varios casos de falla en el servicio por alcantarillado y alumbrado. Le explico el proceso sin costo inicial y reviso con usted la evidencia y los plazos.', 'NO_ELEGIDA', '2026-06-11T06:06:43-05:00', '2026-06-12T00:06:43-05:00'),
  (3, 2, 1, 'Tengo experiencia en demandas de reparación directa contra el municipio por vías en mal estado. Con los antecedentes del punto el caso es sólido; puedo revisarlo esta misma semana.', 'NO_ELEGIDA', '2026-06-11T10:45:19-05:00', '2026-06-14T06:45:19-05:00'),
  (4, 1, 3, 'Trabajo responsabilidad del Estado hace años. Los reportes previos en el mismo sitio ayudan mucho a demostrar que la entidad conocía la falla. Me interesa acompañarlo.', 'NO_ELEGIDA', '2026-06-11T15:06:43-05:00', '2026-06-12T00:06:43-05:00'),
  (5, 2, 4, 'He llevado varios casos de falla en el servicio por alcantarillado y alumbrado. Le explico el proceso sin costo inicial y reviso con usted la evidencia y los plazos.', 'ELEGIDA', '2026-06-12T03:45:19-05:00', '2026-06-14T06:45:19-05:00'),
  (6, 2, 3, 'Trabajo responsabilidad del Estado hace años. Los reportes previos en el mismo sitio ayudan mucho a demostrar que la entidad conocía la falla. Me interesa acompañarlo.', 'NO_ELEGIDA', '2026-06-13T00:45:19-05:00', '2026-06-14T06:45:19-05:00'),
  (7, 3, 1, 'Tengo experiencia en demandas de reparación directa contra el municipio por vías en mal estado. Con los antecedentes del punto el caso es sólido; puedo revisarlo esta misma semana.', 'NO_ELEGIDA', '2026-06-21T15:05:14-05:00', '2026-06-23T12:05:14-05:00'),
  (8, 3, 3, 'He llevado varios casos de falla en el servicio por alcantarillado y alumbrado. Le explico el proceso sin costo inicial y reviso con usted la evidencia y los plazos.', 'NO_ELEGIDA', '2026-06-22T16:05:14-05:00', '2026-06-23T12:05:14-05:00'),
  (9, 3, 2, 'Trabajo responsabilidad del Estado hace años. Los reportes previos en el mismo sitio ayudan mucho a demostrar que la entidad conocía la falla. Me interesa acompañarlo.', 'ELEGIDA', '2026-06-23T00:05:14-05:00', '2026-06-23T12:05:14-05:00'),
  (10, 4, 2, 'Tengo experiencia en demandas de reparación directa contra el municipio por vías en mal estado. Con los antecedentes del punto el caso es sólido; puedo revisarlo esta misma semana.', 'PENDIENTE', '2026-07-23T21:40:00-05:00', NULL),
  (11, 4, 3, 'He llevado varios casos de falla en el servicio por alcantarillado y alumbrado. Le explico el proceso sin costo inicial y reviso con usted la evidencia y los plazos.', 'PENDIENTE', '2026-07-24T11:40:00-05:00', NULL),
  (12, 5, 4, 'Tengo experiencia en demandas de reparación directa contra el municipio por vías en mal estado. Con los antecedentes del punto el caso es sólido; puedo revisarlo esta misma semana.', 'NO_ELEGIDA', '2026-08-04T17:54:15-05:00', '2026-08-07T06:54:15-05:00'),
  (13, 5, 1, 'He llevado varios casos de falla en el servicio por alcantarillado y alumbrado. Le explico el proceso sin costo inicial y reviso con usted la evidencia y los plazos.', 'NO_ELEGIDA', '2026-08-05T10:54:15-05:00', '2026-08-07T06:54:15-05:00'),
  (14, 5, 3, 'Trabajo responsabilidad del Estado hace años. Los reportes previos en el mismo sitio ayudan mucho a demostrar que la entidad conocía la falla. Me interesa acompañarlo.', 'ELEGIDA', '2026-08-06T04:54:15-05:00', '2026-08-07T06:54:15-05:00'),
  (15, 6, 2, 'Tengo experiencia en demandas de reparación directa contra el municipio por vías en mal estado. Con los antecedentes del punto el caso es sólido; puedo revisarlo esta misma semana.', 'NO_ELEGIDA', '2026-08-13T19:50:00-05:00', '2026-08-16T09:50:00-05:00'),
  (16, 6, 3, 'He llevado varios casos de falla en el servicio por alcantarillado y alumbrado. Le explico el proceso sin costo inicial y reviso con usted la evidencia y los plazos.', 'ELEGIDA', '2026-08-14T04:50:00-05:00', '2026-08-16T09:50:00-05:00'),
  (17, 6, 4, 'Trabajo responsabilidad del Estado hace años. Los reportes previos en el mismo sitio ayudan mucho a demostrar que la entidad conocía la falla. Me interesa acompañarlo.', 'NO_ELEGIDA', '2026-08-15T01:50:00-05:00', '2026-08-16T09:50:00-05:00'),
  (18, 7, 2, 'Tengo experiencia en demandas de reparación directa contra el municipio por vías en mal estado. Con los antecedentes del punto el caso es sólido; puedo revisarlo esta misma semana.', 'PENDIENTE', '2026-08-26T08:15:00-05:00', NULL),
  (19, 7, 4, 'He llevado varios casos de falla en el servicio por alcantarillado y alumbrado. Le explico el proceso sin costo inicial y reviso con usted la evidencia y los plazos.', 'PENDIENTE', '2026-08-27T07:15:00-05:00', NULL),
  (20, 7, 3, 'Trabajo responsabilidad del Estado hace años. Los reportes previos en el mismo sitio ayudan mucho a demostrar que la entidad conocía la falla. Me interesa acompañarlo.', 'PENDIENTE', '2026-08-27T12:15:00-05:00', NULL);

INSERT INTO calificaciones (id_calificacion, id_caso, id_abogado, puntaje, comentario, fecha) VALUES
  (1, 1, 1, 4, 'Buen acompañamiento, aunque a veces tardaba en responder los mensajes.', '2026-07-21T00:06:43-05:00'),
  (2, 3, 2, 4, 'Conoce bien estos procesos. Recomendado.', '2026-08-03T12:05:14-05:00');

INSERT INTO remisiones (id_remision, id_caso, id_abogado, fecha_envio, estado_remision, fecha_respuesta, motivo_rechazo, valor_cobrado) VALUES
  (1, 1, 1, '2026-06-12T00:06:43-05:00', 'ACEPTADA', '2026-06-12T00:06:43-05:00', NULL, 200000),
  (2, 2, 4, '2026-06-14T06:45:19-05:00', 'ACEPTADA', '2026-06-14T06:45:19-05:00', NULL, 200000),
  (3, 3, 2, '2026-06-23T12:05:14-05:00', 'ACEPTADA', '2026-06-23T12:05:14-05:00', NULL, 200000),
  (4, 5, 3, '2026-08-07T06:54:15-05:00', 'ACEPTADA', '2026-08-07T06:54:15-05:00', NULL, 120000),
  (5, 6, 3, '2026-08-16T09:50:00-05:00', 'ACEPTADA', '2026-08-16T09:50:00-05:00', NULL, 200000);

