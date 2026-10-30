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
    WHERE h.id_reporte = r.id_reporte ORDER BY h.fecha_cambio DESC, h.id_historial DESC LIMIT 1
) ult ON true
WHERE r.id_estado <> coalesce(ult.id_estado_nuevo, (SELECT id_estado FROM estados WHERE nombre = 'Pendiente'));

\echo '2. El historial encadena: el estado anterior de cada cambio es el nuevo del cambio previo. Esperado: 0'
SELECT count(*) AS saltos
FROM (
    SELECT id_estado_anterior,
           lag(id_estado_nuevo) OVER (PARTITION BY id_reporte ORDER BY id_historial) AS previo
    FROM historial_estados
) t
WHERE previo IS NOT NULL AND previo <> id_estado_anterior;

\echo '3. Ningún cambio de estado es anterior a la creación del reporte. Esperado: 0'
SELECT count(*) FROM historial_estados h JOIN reportes r USING (id_reporte) WHERE h.fecha_cambio < r.fecha_reporte;

\echo '4. Todo reporte INCIDENTE tiene su fila en incidentes (regla que irá en un trigger en la Entrega 3). Esperado: 0'
SELECT count(*) FROM reportes r
WHERE r.tipo_reporte = 'INCIDENTE' AND NOT EXISTS (SELECT 1 FROM incidentes i WHERE i.id_reporte = r.id_reporte);

\echo '5. El hecho ocurre antes de reportarlo. Esperado: 0'
SELECT count(*) FROM incidentes i JOIN reportes r USING (id_reporte) WHERE i.fecha_hecho > r.fecha_reporte;

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
           AND rc.fecha_reporte < i.fecha_hecho
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

\echo '12. Toda remisión ACEPTADA corresponde a la postulación ELEGIDA del mismo abogado. Esperado: 0'
SELECT count(*) FROM remisiones rm
WHERE rm.estado_remision = 'ACEPTADA'
  AND NOT EXISTS (SELECT 1 FROM postulaciones p
                  WHERE p.id_caso = rm.id_caso AND p.id_abogado = rm.id_abogado AND p.estado = 'ELEGIDA');

\echo '13. Casos con abogado: exactamente una postulación elegida; casos buscando abogado: todas pendientes. Esperado: 0'
SELECT count(*) FROM casos c JOIN estados_caso ec USING (id_estado_caso)
WHERE (ec.nombre IN ('Aceptado por abogado', 'Cerrado') AND c.id_caso IN (SELECT id_caso FROM remisiones)
       AND (SELECT count(*) FROM postulaciones p WHERE p.id_caso = c.id_caso AND p.estado = 'ELEGIDA') <> 1)
   OR (ec.nombre = 'Remitido' AND EXISTS (SELECT 1 FROM postulaciones p WHERE p.id_caso = c.id_caso AND p.estado <> 'PENDIENTE'));

\echo '14. Solo se postulan abogados activos (verificados). Esperado: 0'
SELECT count(*) FROM postulaciones p JOIN abogados a USING (id_abogado) WHERE NOT a.activo;

\echo '15. Solo se califica un caso cerrado y al abogado que el ciudadano eligió. Esperado: 0'
SELECT count(*) FROM calificaciones ca
JOIN casos c USING (id_caso) JOIN estados_caso ec USING (id_estado_caso)
WHERE ec.nombre <> 'Cerrado'
   OR NOT EXISTS (SELECT 1 FROM remisiones rm WHERE rm.id_caso = ca.id_caso AND rm.id_abogado = ca.id_abogado
                  AND rm.estado_remision = 'ACEPTADA')
   OR ca.fecha < c.fecha_cierre;

\echo '16. Cada cálculo de índice es posterior a su reporte y su valor es la suma de los aportes del desglose. Esperado: 0'
SELECT count(*) FROM calculos_indice ci JOIN reportes r USING (id_reporte)
WHERE ci.fecha_calculo <= r.fecha_reporte
   OR abs(ci.valor - (SELECT sum((d.value ->> 'aporte')::numeric) FROM jsonb_each(ci.desglose) d)) > 0.01;

\echo '17. Ningún token de recuperación es anterior al registro de su usuario. Esperado: 0'
SELECT count(*) FROM tokens_recuperacion t JOIN usuarios u USING (id_usuario) WHERE t.fecha_creacion < u.fecha_registro;

\echo '18. La semilla incluye notificaciones leídas y pendientes de sus autores. Esperado: 0'
SELECT CASE WHEN count(*) >= 5
                  AND count(*) FILTER (WHERE n.fecha_lectura IS NULL) > 0
                  AND count(*) FILTER (WHERE n.fecha_lectura IS NOT NULL) > 0
                  AND count(*) FILTER (WHERE n.id_usuario <> r.id_usuario) = 0
            THEN 0 ELSE 1 END
FROM notificaciones n
JOIN historial_estados h USING (id_historial)
JOIN reportes r USING (id_reporte);

\echo '19. La semilla tiene bloqueos de prueba sin asociarlos a cuentas reales. Esperado: 0'
SELECT CASE WHEN count(*) >= 5 AND count(*) FILTER (WHERE u.id_usuario IS NOT NULL) = 0
            THEN 0 ELSE 1 END
FROM bloqueos_login b
LEFT JOIN usuarios u ON u.correo = b.correo;

\echo 'Caso demo: antecedentes del incidente más reciente (esperado: 4, el primero el 2026-05-19)'
SELECT i.id_incidente, count(rr.*) AS antecedentes, min(rc.fecha_reporte)::date AS primero
FROM incidentes i
JOIN reportes_relacionados rr ON rr.id_reporte = i.id_reporte AND rr.tipo_relacion = 'ANTECEDENTE'
JOIN reportes rc ON rc.id_reporte = rr.id_reporte_relacionado
WHERE i.id_incidente = (SELECT max(id_incidente) FROM incidentes)
GROUP BY i.id_incidente;
