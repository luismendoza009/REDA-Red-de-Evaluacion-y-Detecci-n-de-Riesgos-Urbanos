-- DATOS SIMULADOS generados por scripts/generar_semilla.py (no editar a mano).
-- Ninguna persona es real. Ver documentacion/datos_simulados.md

INSERT INTO casos (id_caso, id_incidente, id_estado_caso, id_operador, fecha_creacion, fecha_cierre, observacion, fecha_consentimiento) VALUES
  (1, 2, 6, 2, '2026-06-10T12:06:43-05:00', '2026-07-21T14:06:43-05:00', 'Incidente sin antecedentes registrados.', '2026-06-10T12:06:43-05:00'),
  (2, 3, 4, 4, '2026-06-10T15:45:19-05:00', NULL, 'Incidente sin antecedentes registrados.', '2026-06-10T15:45:19-05:00'),
  (3, 5, 6, 2, '2026-06-20T09:05:14-05:00', '2026-07-19T05:05:14-05:00', 'Incidente sin antecedentes registrados.', '2026-06-20T09:05:14-05:00'),
  (4, 7, 3, 4, '2026-07-23T08:40:00-05:00', NULL, 'Incidente con antecedentes previos en el punto.', '2026-07-23T08:40:00-05:00'),
  (5, 10, 6, 3, '2026-08-12T17:50:00-05:00', '2026-09-16T17:50:00-05:00', 'Incidente con antecedentes previos en el punto.', '2026-08-12T17:50:00-05:00'),
  (6, 11, 4, 4, '2026-08-24T19:15:00-05:00', NULL, 'Incidente con antecedentes previos en el punto.', '2026-08-24T19:15:00-05:00'),
  (7, 13, 3, 3, '2026-08-29T19:05:00-05:00', NULL, 'Incidente con antecedentes previos en el punto.', '2026-08-29T19:05:00-05:00'),
  (8, 15, 5, 3, '2026-10-02T18:00:00-05:00', '2026-10-20T18:00:00-05:00', 'Incidente con antecedentes previos en el punto.', '2026-10-02T18:00:00-05:00'),
  (9, 17, 2, 2, '2026-10-24T09:12:58-05:00', NULL, 'Incidente sin antecedentes registrados.', '2026-10-24T09:12:58-05:00');

INSERT INTO postulaciones (id_postulacion, id_caso, id_abogado, mensaje, estado, fecha_postulacion, fecha_resolucion) VALUES
  (1, 1, 2, 'Tengo experiencia en demandas de reparación directa contra el municipio por vías en mal estado. Con los antecedentes del punto el caso es sólido; puedo revisarlo esta misma semana.', 'ELEGIDA', '2026-06-11T05:06:43-05:00', '2026-06-14T14:06:43-05:00'),
  (2, 1, 3, 'He llevado varios casos de falla en el servicio por alcantarillado y alumbrado. Le explico el proceso sin costo inicial y reviso con usted la evidencia y los plazos.', 'NO_ELEGIDA', '2026-06-11T15:06:43-05:00', '2026-06-14T14:06:43-05:00'),
  (3, 2, 3, 'Tengo experiencia en demandas de reparación directa contra el municipio por vías en mal estado. Con los antecedentes del punto el caso es sólido; puedo revisarlo esta misma semana.', 'ELEGIDA', '2026-06-11T15:45:19-05:00', '2026-06-13T23:45:19-05:00'),
  (4, 1, 1, 'Trabajo responsabilidad del Estado hace años. Los reportes previos en el mismo sitio ayudan mucho a demostrar que la entidad conocía la falla. Me interesa acompañarlo.', 'NO_ELEGIDA', '2026-06-12T15:06:43-05:00', '2026-06-14T14:06:43-05:00'),
  (5, 2, 2, 'He llevado varios casos de falla en el servicio por alcantarillado y alumbrado. Le explico el proceso sin costo inicial y reviso con usted la evidencia y los plazos.', 'NO_ELEGIDA', '2026-06-12T21:45:19-05:00', '2026-06-13T23:45:19-05:00'),
  (6, 2, 4, 'Trabajo responsabilidad del Estado hace años. Los reportes previos en el mismo sitio ayudan mucho a demostrar que la entidad conocía la falla. Me interesa acompañarlo.', 'NO_ELEGIDA', '2026-06-13T19:45:19-05:00', '2026-06-13T23:45:19-05:00'),
  (7, 3, 1, 'Tengo experiencia en demandas de reparación directa contra el municipio por vías en mal estado. Con los antecedentes del punto el caso es sólido; puedo revisarlo esta misma semana.', 'NO_ELEGIDA', '2026-06-21T09:05:14-05:00', '2026-06-24T05:05:14-05:00'),
  (8, 3, 4, 'He llevado varios casos de falla en el servicio por alcantarillado y alumbrado. Le explico el proceso sin costo inicial y reviso con usted la evidencia y los plazos.', 'ELEGIDA', '2026-06-22T02:05:14-05:00', '2026-06-24T05:05:14-05:00'),
  (9, 3, 3, 'Trabajo responsabilidad del Estado hace años. Los reportes previos en el mismo sitio ayudan mucho a demostrar que la entidad conocía la falla. Me interesa acompañarlo.', 'NO_ELEGIDA', '2026-06-22T23:05:14-05:00', '2026-06-24T05:05:14-05:00'),
  (10, 4, 3, 'Tengo experiencia en demandas de reparación directa contra el municipio por vías en mal estado. Con los antecedentes del punto el caso es sólido; puedo revisarlo esta misma semana.', 'PENDIENTE', '2026-07-24T09:40:00-05:00', NULL),
  (11, 4, 4, 'He llevado varios casos de falla en el servicio por alcantarillado y alumbrado. Le explico el proceso sin costo inicial y reviso con usted la evidencia y los plazos.', 'PENDIENTE', '2026-07-25T02:40:00-05:00', NULL),
  (12, 4, 1, 'Trabajo responsabilidad del Estado hace años. Los reportes previos en el mismo sitio ayudan mucho a demostrar que la entidad conocía la falla. Me interesa acompañarlo.', 'PENDIENTE', '2026-07-25T09:40:00-05:00', NULL),
  (13, 5, 2, 'Tengo experiencia en demandas de reparación directa contra el municipio por vías en mal estado. Con los antecedentes del punto el caso es sólido; puedo revisarlo esta misma semana.', 'ELEGIDA', '2026-08-13T22:50:00-05:00', '2026-08-14T17:50:00-05:00'),
  (14, 5, 3, 'He llevado varios casos de falla en el servicio por alcantarillado y alumbrado. Le explico el proceso sin costo inicial y reviso con usted la evidencia y los plazos.', 'NO_ELEGIDA', '2026-08-14T12:50:00-05:00', '2026-08-14T17:50:00-05:00'),
  (15, 6, 1, 'Tengo experiencia en demandas de reparación directa contra el municipio por vías en mal estado. Con los antecedentes del punto el caso es sólido; puedo revisarlo esta misma semana.', 'NO_ELEGIDA', '2026-08-25T12:15:00-05:00', '2026-08-28T12:15:00-05:00'),
  (16, 6, 3, 'He llevado varios casos de falla en el servicio por alcantarillado y alumbrado. Le explico el proceso sin costo inicial y reviso con usted la evidencia y los plazos.', 'ELEGIDA', '2026-08-26T00:15:00-05:00', '2026-08-28T12:15:00-05:00'),
  (17, 6, 2, 'Trabajo responsabilidad del Estado hace años. Los reportes previos en el mismo sitio ayudan mucho a demostrar que la entidad conocía la falla. Me interesa acompañarlo.', 'NO_ELEGIDA', '2026-08-26T14:15:00-05:00', '2026-08-28T12:15:00-05:00'),
  (18, 7, 2, 'Tengo experiencia en demandas de reparación directa contra el municipio por vías en mal estado. Con los antecedentes del punto el caso es sólido; puedo revisarlo esta misma semana.', 'PENDIENTE', '2026-08-30T09:05:00-05:00', NULL),
  (19, 7, 3, 'He llevado varios casos de falla en el servicio por alcantarillado y alumbrado. Le explico el proceso sin costo inicial y reviso con usted la evidencia y los plazos.', 'PENDIENTE', '2026-08-30T18:05:00-05:00', NULL),
  (20, 7, 4, 'Trabajo responsabilidad del Estado hace años. Los reportes previos en el mismo sitio ayudan mucho a demostrar que la entidad conocía la falla. Me interesa acompañarlo.', 'PENDIENTE', '2026-08-31T15:05:00-05:00', NULL);

INSERT INTO calificaciones (id_calificacion, id_caso, id_abogado, puntaje, comentario, fecha) VALUES
  (1, 3, 4, 4, 'Conoce bien estos procesos. Recomendado.', '2026-07-22T05:05:14-05:00'),
  (2, 1, 2, 4, 'Buen acompañamiento, aunque a veces tardaba en responder los mensajes.', '2026-07-24T14:06:43-05:00');

INSERT INTO remisiones (id_remision, id_caso, id_abogado, fecha_envio, estado_remision, fecha_respuesta, motivo_rechazo, valor_cobrado) VALUES
  (1, 2, 3, '2026-06-13T23:45:19-05:00', 'ACEPTADA', '2026-06-13T23:45:19-05:00', NULL, 200000),
  (2, 1, 2, '2026-06-14T14:06:43-05:00', 'ACEPTADA', '2026-06-14T14:06:43-05:00', NULL, 200000),
  (3, 3, 4, '2026-06-24T05:05:14-05:00', 'ACEPTADA', '2026-06-24T05:05:14-05:00', NULL, 200000),
  (4, 5, 2, '2026-08-14T17:50:00-05:00', 'ACEPTADA', '2026-08-14T17:50:00-05:00', NULL, 200000),
  (5, 6, 3, '2026-08-28T12:15:00-05:00', 'ACEPTADA', '2026-08-28T12:15:00-05:00', NULL, 200000);

