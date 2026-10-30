-- DATOS SIMULADOS generados por scripts/generar_semilla.py (no editar a mano).
-- Ninguna persona es real. Ver documentacion/datos_simulados.md

INSERT INTO casos (id_caso, id_incidente, id_estado_caso, id_operador, fecha_creacion, fecha_cierre, observacion, fecha_consentimiento) VALUES
  (1, 2, 6, 2, '2026-06-10', '2026-07-19', 'Incidente con antecedentes previos en el punto.', '2026-06-10'),
  (2, 3, 6, 4, '2026-06-10', '2026-07-19', 'Conciliación con la entidad; el caso termina.', '2026-06-10'),
  (3, 5, 6, 2, '2026-06-20', '2026-08-01', 'Incidente sin antecedentes registrados.', '2026-06-20'),
  (4, 7, 3, 4, '2026-07-22', NULL, 'Incidente con antecedentes previos en el punto.', '2026-07-22'),
  (5, 9, 6, 4, '2026-08-04', '2026-09-05', 'Incidente sin antecedentes registrados.', '2026-08-04'),
  (6, 10, 4, 3, '2026-08-13', NULL, 'Incidente con antecedentes previos en el punto.', '2026-08-13'),
  (7, 11, 3, 4, '2026-08-26', NULL, 'Incidente con antecedentes previos en el punto.', '2026-08-26'),
  (8, 13, 5, 3, '2026-08-30', '2026-09-29', 'Incidente con antecedentes previos en el punto.', '2026-08-30'),
  (9, 15, 2, 3, '2026-10-01', NULL, 'Incidente con antecedentes previos en el punto.', '2026-10-01'),
  (10, 6, 6, 3, '2026-07-19', '2026-08-20', 'Conciliación con la entidad; el caso termina.', '2026-07-19');

INSERT INTO postulaciones (id_postulacion, id_caso, id_abogado, mensaje, estado, fecha_postulacion, fecha_resolucion) VALUES
  (1, 1, 1, 'Tengo experiencia en demandas de reparación directa contra el municipio por vías en mal estado. Con los antecedentes del punto el caso es sólido; puedo revisarlo esta misma semana.', 'ELEGIDA', '2026-06-10', '2026-06-12'),
  (2, 1, 4, 'He llevado varios casos de falla en el servicio por alcantarillado y alumbrado. Le explico el proceso sin costo inicial y reviso con usted la evidencia y los plazos.', 'NO_ELEGIDA', '2026-06-11', '2026-06-12'),
  (3, 2, 1, 'Tengo experiencia en demandas de reparación directa contra el municipio por vías en mal estado. Con los antecedentes del punto el caso es sólido; puedo revisarlo esta misma semana.', 'NO_ELEGIDA', '2026-06-11', '2026-06-14'),
  (4, 1, 3, 'Trabajo responsabilidad del Estado hace años. Los reportes previos en el mismo sitio ayudan mucho a demostrar que la entidad conocía la falla. Me interesa acompañarlo.', 'NO_ELEGIDA', '2026-06-11', '2026-06-12'),
  (5, 2, 4, 'He llevado varios casos de falla en el servicio por alcantarillado y alumbrado. Le explico el proceso sin costo inicial y reviso con usted la evidencia y los plazos.', 'ELEGIDA', '2026-06-12', '2026-06-14'),
  (6, 2, 3, 'Trabajo responsabilidad del Estado hace años. Los reportes previos en el mismo sitio ayudan mucho a demostrar que la entidad conocía la falla. Me interesa acompañarlo.', 'NO_ELEGIDA', '2026-06-13', '2026-06-14'),
  (7, 3, 1, 'Tengo experiencia en demandas de reparación directa contra el municipio por vías en mal estado. Con los antecedentes del punto el caso es sólido; puedo revisarlo esta misma semana.', 'NO_ELEGIDA', '2026-06-21', '2026-06-23'),
  (8, 3, 3, 'He llevado varios casos de falla en el servicio por alcantarillado y alumbrado. Le explico el proceso sin costo inicial y reviso con usted la evidencia y los plazos.', 'NO_ELEGIDA', '2026-06-22', '2026-06-23'),
  (9, 3, 2, 'Trabajo responsabilidad del Estado hace años. Los reportes previos en el mismo sitio ayudan mucho a demostrar que la entidad conocía la falla. Me interesa acompañarlo.', 'ELEGIDA', '2026-06-23', '2026-06-23'),
  (10, 4, 2, 'Tengo experiencia en demandas de reparación directa contra el municipio por vías en mal estado. Con los antecedentes del punto el caso es sólido; puedo revisarlo esta misma semana.', 'PENDIENTE', '2026-07-23', NULL),
  (11, 4, 3, 'He llevado varios casos de falla en el servicio por alcantarillado y alumbrado. Le explico el proceso sin costo inicial y reviso con usted la evidencia y los plazos.', 'PENDIENTE', '2026-07-24', NULL),
  (12, 5, 4, 'Tengo experiencia en demandas de reparación directa contra el municipio por vías en mal estado. Con los antecedentes del punto el caso es sólido; puedo revisarlo esta misma semana.', 'NO_ELEGIDA', '2026-08-04', '2026-08-07'),
  (13, 5, 1, 'He llevado varios casos de falla en el servicio por alcantarillado y alumbrado. Le explico el proceso sin costo inicial y reviso con usted la evidencia y los plazos.', 'NO_ELEGIDA', '2026-08-05', '2026-08-07'),
  (14, 5, 3, 'Trabajo responsabilidad del Estado hace años. Los reportes previos en el mismo sitio ayudan mucho a demostrar que la entidad conocía la falla. Me interesa acompañarlo.', 'ELEGIDA', '2026-08-06', '2026-08-07'),
  (15, 6, 2, 'Tengo experiencia en demandas de reparación directa contra el municipio por vías en mal estado. Con los antecedentes del punto el caso es sólido; puedo revisarlo esta misma semana.', 'NO_ELEGIDA', '2026-08-13', '2026-08-16'),
  (16, 6, 3, 'He llevado varios casos de falla en el servicio por alcantarillado y alumbrado. Le explico el proceso sin costo inicial y reviso con usted la evidencia y los plazos.', 'ELEGIDA', '2026-08-14', '2026-08-16'),
  (17, 6, 4, 'Trabajo responsabilidad del Estado hace años. Los reportes previos en el mismo sitio ayudan mucho a demostrar que la entidad conocía la falla. Me interesa acompañarlo.', 'NO_ELEGIDA', '2026-08-15', '2026-08-16'),
  (18, 7, 2, 'Tengo experiencia en demandas de reparación directa contra el municipio por vías en mal estado. Con los antecedentes del punto el caso es sólido; puedo revisarlo esta misma semana.', 'PENDIENTE', '2026-08-26', NULL),
  (19, 7, 4, 'He llevado varios casos de falla en el servicio por alcantarillado y alumbrado. Le explico el proceso sin costo inicial y reviso con usted la evidencia y los plazos.', 'PENDIENTE', '2026-08-27', NULL),
  (20, 7, 3, 'Trabajo responsabilidad del Estado hace años. Los reportes previos en el mismo sitio ayudan mucho a demostrar que la entidad conocía la falla. Me interesa acompañarlo.', 'PENDIENTE', '2026-08-27', NULL),
  (21, 10, 1, 'Tengo experiencia en demandas de reparación directa contra el municipio por vías en mal estado. Con los antecedentes del punto el caso es sólido; puedo revisarlo esta misma semana.', 'ELEGIDA', '2026-07-19', '2026-07-21'),
  (22, 10, 4, 'He llevado varios casos de falla en el servicio por alcantarillado y alumbrado. Le explico el proceso sin costo inicial y reviso con usted la evidencia y los plazos.', 'NO_ELEGIDA', '2026-07-20', '2026-07-21');

INSERT INTO calificaciones (id_calificacion, id_caso, id_abogado, puntaje, comentario, fecha) VALUES
  (1, 1, 1, 4, 'Buen acompañamiento, aunque a veces tardaba en responder los mensajes.', '2026-07-21'),
  (2, 3, 2, 4, 'Conoce bien estos procesos. Recomendado.', '2026-08-03'),
  (3, 2, 4, 5, 'Muy claro desde la primera cita; me explicó cada paso y estuvo pendiente todo el tiempo.', '2026-07-21'),
  (4, 5, 3, 3, 'Cumplió, pero esperaba más comunicación durante el proceso.', '2026-09-07'),
  (5, 10, 1, 5, 'Excelente. Usó los reportes previos del punto y la entidad aceptó conciliar.', '2026-08-22');

INSERT INTO remisiones (id_remision, id_caso, id_abogado, fecha_envio, estado_remision, fecha_respuesta, motivo_rechazo, valor_cobrado) VALUES
  (1, 1, 1, '2026-06-12', 'ACEPTADA', '2026-06-12', NULL, 200000),
  (2, 2, 4, '2026-06-14', 'ACEPTADA', '2026-06-14', NULL, 200000),
  (3, 3, 2, '2026-06-23', 'ACEPTADA', '2026-06-23', NULL, 200000),
  (4, 5, 3, '2026-08-07', 'ACEPTADA', '2026-08-07', NULL, 120000),
  (5, 6, 3, '2026-08-16', 'ACEPTADA', '2026-08-16', NULL, 200000),
  (6, 10, 1, '2026-07-21', 'ACEPTADA', '2026-07-21', NULL, 120000);

