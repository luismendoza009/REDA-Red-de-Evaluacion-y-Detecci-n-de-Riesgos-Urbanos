-- La semilla inserta IDs explícitos; se ajustan las secuencias para que los
-- siguientes INSERT de la aplicación no choquen con ellos.
DO $$
BEGIN
  PERFORM setval(pg_get_serial_sequence('roles', 'id_rol'), coalesce((SELECT max(id_rol) FROM roles), 0) + 1, false);
  PERFORM setval(pg_get_serial_sequence('estados', 'id_estado'), coalesce((SELECT max(id_estado) FROM estados), 0) + 1, false);
  PERFORM setval(pg_get_serial_sequence('estados_caso', 'id_estado_caso'), coalesce((SELECT max(id_estado_caso) FROM estados_caso), 0) + 1, false);
  PERFORM setval(pg_get_serial_sequence('categorias', 'id_categoria'), coalesce((SELECT max(id_categoria) FROM categorias), 0) + 1, false);
  PERFORM setval(pg_get_serial_sequence('zonas', 'id_zona'), coalesce((SELECT max(id_zona) FROM zonas), 0) + 1, false);
  PERFORM setval(pg_get_serial_sequence('entidades_publicas', 'id_entidad'), coalesce((SELECT max(id_entidad) FROM entidades_publicas), 0) + 1, false);
  PERFORM setval(pg_get_serial_sequence('usuarios', 'id_usuario'), coalesce((SELECT max(id_usuario) FROM usuarios), 0) + 1, false);
  PERFORM setval(pg_get_serial_sequence('abogados', 'id_abogado'), coalesce((SELECT max(id_abogado) FROM abogados), 0) + 1, false);
  PERFORM setval(pg_get_serial_sequence('reportes', 'id_reporte'), coalesce((SELECT max(id_reporte) FROM reportes), 0) + 1, false);
  PERFORM setval(pg_get_serial_sequence('evidencias', 'id_evidencia'), coalesce((SELECT max(id_evidencia) FROM evidencias), 0) + 1, false);
  PERFORM setval(pg_get_serial_sequence('evaluaciones', 'id_evaluacion'), coalesce((SELECT max(id_evaluacion) FROM evaluaciones), 0) + 1, false);
  PERFORM setval(pg_get_serial_sequence('historial_estados', 'id_historial'), coalesce((SELECT max(id_historial) FROM historial_estados), 0) + 1, false);
  PERFORM setval(pg_get_serial_sequence('incidentes', 'id_incidente'), coalesce((SELECT max(id_incidente) FROM incidentes), 0) + 1, false);
  PERFORM setval(pg_get_serial_sequence('testigos', 'id_testigo'), coalesce((SELECT max(id_testigo) FROM testigos), 0) + 1, false);
  PERFORM setval(pg_get_serial_sequence('casos', 'id_caso'), coalesce((SELECT max(id_caso) FROM casos), 0) + 1, false);
  PERFORM setval(pg_get_serial_sequence('remisiones', 'id_remision'), coalesce((SELECT max(id_remision) FROM remisiones), 0) + 1, false);
  PERFORM setval(pg_get_serial_sequence('versiones_indice', 'id_version'), coalesce((SELECT max(id_version) FROM versiones_indice), 0) + 1, false);
  PERFORM setval(pg_get_serial_sequence('calculos_indice', 'id_calculo'), coalesce((SELECT max(id_calculo) FROM calculos_indice), 0) + 1, false);
  PERFORM setval(pg_get_serial_sequence('postulaciones', 'id_postulacion'), coalesce((SELECT max(id_postulacion) FROM postulaciones), 0) + 1, false);
  PERFORM setval(pg_get_serial_sequence('tokens_recuperacion', 'id_token'), coalesce((SELECT max(id_token) FROM tokens_recuperacion), 0) + 1, false);
END $$;
