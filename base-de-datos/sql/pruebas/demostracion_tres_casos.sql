-- =============================================================================
-- demostracion_tres_casos.sql
-- Guion de la sustentación: muestra con los datos de prueba los tres casos que pide la rúbrica.
--   1. Una fila que se relaciona con VARIAS.
--   2. Una fila que NO se relaciona con NINGUNA.
--   3. El servidor RECHAZANDO datos (con su mensaje), por restricciones que expresan reglas del negocio.
-- No modifica nada: las consultas solo leen y las sentencias del punto 3 las rechaza la base.
-- Ejecutar (con semilla cargada):  psql -U postgres -d reda -f sql/pruebas/demostracion_tres_casos.sql
-- =============================================================================
\set ON_ERROR_STOP off
\pset footer off

\echo
\echo '=== 1. FILAS QUE SE RELACIONAN CON VARIAS ==='
\echo
\echo '1a. El incidente demo (reporte 137) tiene 4 reportes previos en el mismo punto: "la entidad ya sabía".'
SELECT rr.id_reporte              AS incidente,
       rc.id_reporte              AS antecedente,
       rc.fecha_reporte::date     AS reportado_el,
       e.nombre                   AS estado_antecedente
FROM reportes_relacionados rr
JOIN reportes rc ON rc.id_reporte = rr.id_reporte_relacionado
JOIN estados  e  ON e.id_estado   = rc.id_estado
WHERE rr.id_reporte = 137 AND rr.tipo_relacion = 'ANTECEDENTE'
ORDER BY rc.fecha_reporte;

\echo '1b. Un abogado se postula a varios casos y un caso recibe varias postulaciones (M:N resuelta con postulaciones).'
SELECT a.id_abogado, u.nombre AS abogado, count(*) AS casos_a_los_que_se_postulo
FROM postulaciones p
JOIN abogados a USING (id_abogado)
JOIN usuarios u USING (id_usuario)
GROUP BY a.id_abogado, u.nombre
ORDER BY casos_a_los_que_se_postulo DESC;

SELECT id_caso, count(*) AS abogados_postulados
FROM postulaciones GROUP BY id_caso HAVING count(*) > 1 ORDER BY id_caso;

\echo
\echo '=== 2. FILAS QUE NO SE RELACIONAN CON NINGUNA ==='
\echo
\echo '2a. Zonas sin ningún reporte (se ven gracias al LEFT JOIN).'
SELECT z.id_zona, z.nombre, count(r.id_reporte) AS reportes
FROM zonas z LEFT JOIN reportes r USING (id_zona)
GROUP BY z.id_zona, z.nombre
HAVING count(r.id_reporte) = 0;

\echo '2b. Abogados sin ninguna postulación (el pendiente de verificación no puede postularse).'
SELECT a.id_abogado, u.correo, a.activo AS verificado
FROM abogados a JOIN usuarios u USING (id_usuario)
WHERE NOT EXISTS (SELECT 1 FROM postulaciones p WHERE p.id_abogado = a.id_abogado);

\echo '2c. Ciudadanos registrados que todavía no han hecho ningún reporte.'
SELECT u.id_usuario, u.correo
FROM usuarios u JOIN roles ro USING (id_rol)
WHERE ro.nombre = 'CIUDADANO'
  AND NOT EXISTS (SELECT 1 FROM reportes r WHERE r.id_usuario = u.id_usuario);

\echo
\echo '=== 3. EL SERVIDOR RECHAZA DATOS (cada sentencia DEBE terminar en ERROR) ==='
\echo
\echo '3a. Regla: la gravedad que da el ciudadano va de 1 a 5  ->  ck_reportes_gravedad'
INSERT INTO reportes (id_usuario, id_categoria, id_zona, id_estado, descripcion, latitud, longitud, nivel_gravedad)
VALUES ((SELECT min(id_usuario) FROM usuarios WHERE id_rol = 1), 1, 1, 1,
        'Hueco enorme frente al colegio', 10.4631, -73.2532, 7);

\echo '3b. Regla: un reporte es evidencia; no se borra la categoría que lo clasifica  ->  fk_reportes_categoria (ON DELETE RESTRICT)'
DELETE FROM categorias WHERE id_categoria = 1;

\echo '3c. Regla: un caso solo lo puede aceptar UN abogado  ->  uq_remisiones_una_aceptada'
INSERT INTO remisiones (id_caso, id_abogado, estado_remision, fecha_envio, fecha_respuesta, valor_cobrado)
SELECT rm.id_caso, a.id_abogado, 'ACEPTADA', now(), now(), 150000
FROM remisiones rm, abogados a
WHERE rm.estado_remision = 'ACEPTADA'
  AND a.id_abogado NOT IN (SELECT id_abogado FROM remisiones x WHERE x.id_caso = rm.id_caso)
LIMIT 1;

\echo '3d. Regla: solo un reporte de tipo INCIDENTE puede tener datos de daño  ->  fk_incidentes_reporte (FK compuesta)'
INSERT INTO incidentes (id_reporte, fecha_hora_hecho, tipo_dano, descripcion_dano)
VALUES ((SELECT min(id_reporte) FROM reportes WHERE tipo_reporte = 'CONDICION'), now(), 'LESION', 'Caída del peatón en el hueco');

\echo
\echo 'Fin: las cuatro sentencias del punto 3 deben haber mostrado ERROR con el nombre de su restricción.'
