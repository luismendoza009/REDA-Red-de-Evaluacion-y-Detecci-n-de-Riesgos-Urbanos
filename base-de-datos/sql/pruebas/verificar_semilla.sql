-- =============================================================================
-- verificar_semilla.sql
-- Comprobaciones de coherencia de los datos semilla que las restricciones
-- declarativas no pueden expresar. Cada consulta debe devolver 0 filas / 0.
-- Ejecutar: psql -d reda -f sql/pruebas/verificar_semilla.sql
-- =============================================================================

\echo '1. Estado actual del reporte = último estado del historial (o Pendiente si no tiene historial). Esperado: 0'
SELECT count(*) AS inconsistentes
FROM reportes r
LEFT JOIN LATERAL (
    SELECT h.id_estado_nuevo FROM historial_estados h
    WHERE h.id_reporte = r.id_reporte ORDER BY h.fecha_cambio DESC LIMIT 1
) ult ON true
WHERE r.id_estado <> coalesce(ult.id_estado_nuevo, (SELECT id_estado FROM estados WHERE nombre = 'Pendiente'));

\echo '2. El historial encadena: el estado anterior de cada cambio es el nuevo del cambio previo. Esperado: 0'
SELECT count(*) AS saltos
FROM (
    SELECT id_estado_anterior,
           lag(id_estado_nuevo) OVER (PARTITION BY id_reporte ORDER BY fecha_cambio) AS previo
    FROM historial_estados
) t
WHERE previo IS NOT NULL AND previo <> id_estado_anterior;

\echo '3. Ningún cambio de estado es anterior a la creación del reporte. Esperado: 0'
SELECT count(*) FROM historial_estados h JOIN reportes r USING (id_reporte) WHERE h.fecha_cambio < r.fecha_reporte;

\echo '4. Todo reporte INCIDENTE tiene su fila en incidentes (regla que irá en un trigger en la Entrega 3). Esperado: 0'
SELECT count(*) FROM reportes r
WHERE r.tipo_reporte = 'INCIDENTE' AND NOT EXISTS (SELECT 1 FROM incidentes i WHERE i.id_reporte = r.id_reporte);

\echo '5. El hecho ocurre antes de reportarlo. Esperado: 0'
SELECT count(*) FROM incidentes i JOIN reportes r USING (id_reporte) WHERE i.fecha_hora_hecho > r.fecha_reporte;

\echo '6. Los ANTECEDENTE son condiciones NO rechazadas de la misma categoría, a <= 100 m y anteriores al hecho. Esperado: 0'
SELECT count(*)
FROM reportes_relacionados rr
JOIN reportes ri   ON ri.id_reporte = rr.id_reporte
JOIN incidentes i  ON i.id_reporte  = ri.id_reporte
JOIN reportes rc   ON rc.id_reporte = rr.id_reporte_relacionado
WHERE rr.tipo_relacion = 'ANTECEDENTE'
  AND NOT (rc.tipo_reporte = 'CONDICION'
           AND rc.id_estado <> (SELECT id_estado FROM estados WHERE nombre = 'Rechazado')
           AND rc.id_categoria = ri.id_categoria
           AND rc.fecha_reporte < i.fecha_hora_hecho
           AND 2 * 6371000 * asin(sqrt(
                 power(sin(radians(rc.latitud - ri.latitud) / 2), 2) +
                 cos(radians(ri.latitud)) * cos(radians(rc.latitud)) *
                 power(sin(radians(rc.longitud - ri.longitud) / 2), 2))) <= 100);

\echo '7. Un reporte no lo crea alguien registrado después. Esperado: 0'
SELECT count(*) FROM reportes r JOIN usuarios u USING (id_usuario) WHERE u.fecha_registro > r.fecha_reporte;

\echo '8. Los casos solo se abren sobre incidentes validados y después de la validación. Esperado: 0'
SELECT count(*) FROM casos c
JOIN incidentes i USING (id_incidente)
WHERE NOT EXISTS (SELECT 1 FROM historial_estados h JOIN estados e ON e.id_estado = h.id_estado_nuevo
                  WHERE h.id_reporte = i.id_reporte AND e.nombre = 'Validado' AND h.fecha_cambio <= c.fecha_creacion);

\echo '9. Los pesos de cada versión de índice suman 1 (regla que irá en un trigger en la Entrega 3). Esperado: 0'
SELECT count(*) FROM (SELECT id_version FROM parametros_indice GROUP BY id_version HAVING sum(peso) <> 1) t;

\echo '10. Remisiones: se envían después de abrir el caso. Esperado: 0'
SELECT count(*) FROM remisiones rm JOIN casos c USING (id_caso) WHERE rm.fecha_envio < c.fecha_creacion;

\echo '11. Solo evalúan y cambian estados usuarios OPERADOR o ADMINISTRADOR. Esperado: 0'
SELECT count(*) FROM (
    SELECT id_usuario FROM historial_estados UNION ALL SELECT id_usuario FROM evaluaciones UNION ALL SELECT id_operador FROM casos
) x JOIN usuarios u USING (id_usuario) JOIN roles ro USING (id_rol)
WHERE ro.nombre NOT IN ('OPERADOR', 'ADMINISTRADOR');

\echo 'Caso demo: antecedentes del incidente más reciente (esperado: 4, el primero el 2026-05-19)'
SELECT i.id_incidente, count(rr.*) AS antecedentes, min(rc.fecha_reporte)::date AS primero
FROM incidentes i
JOIN reportes_relacionados rr ON rr.id_reporte = i.id_reporte AND rr.tipo_relacion = 'ANTECEDENTE'
JOIN reportes rc ON rc.id_reporte = rr.id_reporte_relacionado
WHERE i.id_incidente = (SELECT max(id_incidente) FROM incidentes)
GROUP BY i.id_incidente;
