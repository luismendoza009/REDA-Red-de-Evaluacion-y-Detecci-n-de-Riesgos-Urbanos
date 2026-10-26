-- =============================================================================
-- probar_restricciones.sql
-- Pruebas negativas: cada bloque intenta violar una regla de negocio y DEBE fallar.
-- Si la base acepta el dato inválido, el bloque lanza 'FALLA: ...' y el script se detiene.
-- Todo corre dentro de una transacción que se deshace al final: no deja basura.
-- Ejecutar (con semilla cargada): psql -d reda -v ON_ERROR_STOP=1 -f sql/pruebas/probar_restricciones.sql
-- =============================================================================
BEGIN;

-- Además de exigir el error, exige que lo haya lanzado la restricción esperada: así una
-- prueba no pasa "por accidente" por culpa de otra regla.
CREATE FUNCTION pg_temp.debe_fallar(descripcion TEXT, restriccion TEXT, sentencia TEXT) RETURNS TEXT AS $$
DECLARE
    filas INT;
    nombre TEXT;
BEGIN
    BEGIN
        EXECUTE sentencia;
        GET DIAGNOSTICS filas = ROW_COUNT;
    EXCEPTION WHEN integrity_constraint_violation OR check_violation OR unique_violation
                   OR foreign_key_violation OR not_null_violation OR restrict_violation THEN
        GET STACKED DIAGNOSTICS nombre = CONSTRAINT_NAME;
        IF nombre IS DISTINCT FROM restriccion THEN
            RAISE EXCEPTION 'FALLA: % lo detuvo % y se esperaba %', descripcion, nombre, restriccion;
        END IF;
        RETURN 'OK  ' || rpad(restriccion, 30) || descripcion;
    END;
    RAISE EXCEPTION 'FALLA: la base aceptó un dato inválido (% filas): %', filas, descripcion;
END;
$$ LANGUAGE plpgsql;

SELECT pg_temp.debe_fallar('incidente colgado de un reporte de CONDICION (FK compuesta)', 'fk_incidentes_reporte',
  $q$INSERT INTO incidentes (id_reporte, fecha_hora_hecho, tipo_dano, descripcion_dano)
     VALUES ((SELECT min(id_reporte) FROM reportes WHERE tipo_reporte = 'CONDICION'), now(), 'LESION', 'Caída del peatón en el hueco')$q$)
UNION ALL
SELECT pg_temp.debe_fallar('cambiar a CONDICION un reporte que ya tiene incidente', 'fk_incidentes_reporte',
  $q$UPDATE reportes SET tipo_reporte = 'CONDICION' WHERE id_reporte = (SELECT min(id_reporte) FROM incidentes)$q$)
UNION ALL
SELECT pg_temp.debe_fallar('dos remisiones ACEPTADAS para el mismo caso (índice único parcial)', 'uq_remisiones_una_aceptada',
  $q$INSERT INTO remisiones (id_caso, id_abogado, estado_remision, fecha_envio, fecha_respuesta, valor_cobrado)
     SELECT rm.id_caso, a.id_abogado, 'ACEPTADA', now(), now(), 150000
     FROM remisiones rm, abogados a
     WHERE rm.estado_remision = 'ACEPTADA'
       AND a.id_abogado NOT IN (SELECT id_abogado FROM remisiones x WHERE x.id_caso = rm.id_caso)
     LIMIT 1$q$)
UNION ALL
SELECT pg_temp.debe_fallar('remisión ACEPTADA sin fecha de respuesta', 'ck_remisiones_respuesta',
  $q$UPDATE remisiones SET fecha_respuesta = NULL WHERE id_remision = (SELECT min(id_remision) FROM remisiones WHERE estado_remision = 'ACEPTADA')$q$)
UNION ALL
SELECT pg_temp.debe_fallar('atención médica en un daño solo material', 'ck_incidentes_atencion',
  $q$UPDATE incidentes SET hubo_atencion_medica = true WHERE tipo_dano = 'DANO_MATERIAL'$q$)
UNION ALL
SELECT pg_temp.debe_fallar('contraseña en texto plano', 'ck_usuarios_hash',
  $q$INSERT INTO usuarios (nombre, correo, password_hash, id_rol) VALUES ('Prueba', 'prueba@correo.test', 'clave123', 1)$q$)
UNION ALL
SELECT pg_temp.debe_fallar('correo con mayúsculas (duplicado encubierto)', 'ck_usuarios_correo',
  $q$INSERT INTO usuarios (nombre, correo, password_hash, id_rol)
     SELECT 'Prueba', upper(correo), password_hash, 1 FROM usuarios LIMIT 1$q$)
UNION ALL
SELECT pg_temp.debe_fallar('nivel de gravedad fuera de 1..5', 'ck_reportes_gravedad',
  $q$UPDATE reportes SET nivel_gravedad = 7 WHERE id_reporte = 1$q$)
UNION ALL
SELECT pg_temp.debe_fallar('coordenada imposible', 'ck_reportes_latitud',
  $q$UPDATE reportes SET latitud = 123.0 WHERE id_reporte = 1$q$)
UNION ALL
SELECT pg_temp.debe_fallar('borrar una categoría con reportes (RESTRICT: el reporte es evidencia)', 'fk_reportes_categoria',
  $q$DELETE FROM categorias WHERE id_categoria = (SELECT id_categoria FROM reportes LIMIT 1)$q$)
UNION ALL
SELECT pg_temp.debe_fallar('borrar un usuario autor de reportes (RESTRICT)', 'fk_reportes_usuario',
  $q$DELETE FROM usuarios WHERE id_usuario = (SELECT id_usuario FROM reportes LIMIT 1)$q$)
UNION ALL
SELECT pg_temp.debe_fallar('un reporte relacionado consigo mismo', 'ck_relacionados_distintos',
  $q$INSERT INTO reportes_relacionados (id_reporte, id_reporte_relacionado) VALUES (1, 1)$q$)
UNION ALL
SELECT pg_temp.debe_fallar('cambio de estado a sí mismo en el historial', 'ck_historial_distintos',
  $q$INSERT INTO historial_estados (id_reporte, id_estado_anterior, id_estado_nuevo, id_usuario) VALUES (1, 2, 2, 2)$q$)
UNION ALL
SELECT pg_temp.debe_fallar('evidencia que no es imagen', 'ck_evidencias_mime',
  $q$INSERT INTO evidencias (id_reporte, ruta_imagen, tipo_mime, tamano_bytes, hash_sha256)
     VALUES (1, 'evidencias/x.exe', 'application/x-msdownload', 100, repeat('a', 64))$q$)
UNION ALL
SELECT pg_temp.debe_fallar('la misma foto (mismo hash) cargada dos veces', 'uq_evidencias_hash',
  $q$INSERT INTO evidencias (id_reporte, ruta_imagen, tipo_mime, tamano_bytes, hash_sha256)
     SELECT id_reporte, ruta_imagen || '.copia', tipo_mime, tamano_bytes, hash_sha256 FROM evidencias LIMIT 1$q$)
UNION ALL
SELECT pg_temp.debe_fallar('dos versiones vigentes del IRU', 'uq_versiones_una_vigente',
  $q$INSERT INTO versiones_indice (nombre_indice, numero_version, descripcion, vigente) VALUES ('IRU', 2, 'prueba', true)$q$)
UNION ALL
SELECT pg_temp.debe_fallar('umbrales de nivel desordenados', 'ck_versiones_umbrales',
  $q$INSERT INTO versiones_indice (nombre_indice, numero_version, descripcion, umbral_medio, umbral_alto) VALUES ('IVC', 9, 'prueba', 60, 40)$q$)
UNION ALL
SELECT pg_temp.debe_fallar('segundo caso para el mismo incidente', 'uq_casos_incidente',
  $q$INSERT INTO casos (id_incidente, id_estado_caso, id_operador) SELECT id_incidente, 1, id_operador FROM casos LIMIT 1$q$)
UNION ALL
SELECT pg_temp.debe_fallar('dos postulaciones ELEGIDAS en el mismo caso', 'uq_postulaciones_una_elegida',
  $q$UPDATE postulaciones SET estado = 'ELEGIDA', fecha_resolucion = now()
     WHERE id_caso = (SELECT id_caso FROM postulaciones WHERE estado = 'ELEGIDA' LIMIT 1)$q$)
UNION ALL
SELECT pg_temp.debe_fallar('el mismo abogado se postula dos veces al mismo caso', 'uq_postulaciones_caso_abogado',
  $q$INSERT INTO postulaciones (id_caso, id_abogado, mensaje)
     SELECT id_caso, id_abogado, mensaje FROM postulaciones LIMIT 1$q$)
UNION ALL
SELECT pg_temp.debe_fallar('postulación con un mensaje sin contenido', 'ck_postulaciones_mensaje',
  $q$INSERT INTO postulaciones (id_caso, id_abogado, mensaje) VALUES (1, 4, 'Me interesa')$q$)
UNION ALL
SELECT pg_temp.debe_fallar('postulación resuelta sin fecha de resolución', 'ck_postulaciones_resolucion',
  $q$UPDATE postulaciones SET estado = 'RETIRADA' WHERE estado = 'PENDIENTE'$q$)
UNION ALL
SELECT pg_temp.debe_fallar('token de recuperación guardado en claro (no es un hash)', 'ck_tokens_hash',
  $q$INSERT INTO tokens_recuperacion (id_usuario, hash_token, fecha_expiracion) VALUES (1, 'MiTokenSecreto', now() + interval '30 minutes')$q$)
UNION ALL
SELECT pg_temp.debe_fallar('token que vence antes de crearse', 'ck_tokens_vigencia',
  $q$INSERT INTO tokens_recuperacion (id_usuario, hash_token, fecha_expiracion) VALUES (1, repeat('b', 64), now() - interval '1 minute')$q$)
UNION ALL
SELECT pg_temp.debe_fallar('calificación de 6 estrellas', 'ck_calificaciones_puntaje',
  $q$UPDATE calificaciones SET puntaje = 6 WHERE id_calificacion = (SELECT min(id_calificacion) FROM calificaciones)$q$)
UNION ALL
SELECT pg_temp.debe_fallar('calificar dos veces el mismo caso', 'uq_calificaciones_caso',
  $q$INSERT INTO calificaciones (id_caso, id_abogado, puntaje) SELECT id_caso, id_abogado, 5 FROM calificaciones LIMIT 1$q$)
UNION ALL
SELECT pg_temp.debe_fallar('evaluación con todos los puntajes vacíos', 'ck_evaluaciones_alguna',
  $q$INSERT INTO evaluaciones (id_reporte, id_usuario) VALUES (1, 2)$q$);

ROLLBACK;
