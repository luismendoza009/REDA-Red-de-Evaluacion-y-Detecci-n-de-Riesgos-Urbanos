-- =============================================================================
-- verificacion_consultas.sql
-- Verifica cada consulta del banco recalculando su resultado por un camino DISTINTO
-- (otra técnica SQL u otra tabla). Cada fila debe mostrar coincide = t.
-- =============================================================================

\echo 'Verificación cruzada del banco de consultas (todas deben ser t)'

-- C01: una fila por incidente, ni más (JOIN que duplica) ni menos (JOIN que pierde filas)
SELECT 'C01 una fila por incidente' AS verificacion,
       (SELECT count(*) FROM incidentes i JOIN reportes r USING (id_reporte)
          JOIN categorias USING (id_categoria) JOIN zonas USING (id_zona) JOIN estados e ON e.id_estado = r.id_estado
          LEFT JOIN entidades_publicas ep ON ep.id_entidad = i.id_entidad)
       = (SELECT count(*) FROM incidentes) AS coincide

UNION ALL
-- C02: sumar por abogado da lo mismo que sumar todas las remisiones aceptadas,
-- y el total de remisiones es el mismo que el total cobrado por caso en la vista
SELECT 'C02 facturado por abogado = total aceptado = total de la vista',
       (SELECT sum(t) FROM (SELECT sum(valor_cobrado) t FROM remisiones WHERE estado_remision = 'ACEPTADA' GROUP BY id_abogado) x)
       = (SELECT sum(valor_cobrado) FROM remisiones WHERE estado_remision = 'ACEPTADA')
   AND (SELECT sum(valor_cobrado) FROM remisiones) = (SELECT sum(total_cobrado) FROM v_resumen_casos)

UNION ALL
-- C03: anti-join con LEFT JOIN vs NOT EXISTS
SELECT 'C03 LEFT JOIN ... IS NULL = NOT EXISTS',
       (SELECT count(*) FROM incidentes i JOIN reportes r USING (id_reporte) JOIN estados e USING (id_estado)
          LEFT JOIN casos ca ON ca.id_incidente = i.id_incidente
        WHERE ca.id_caso IS NULL AND e.nombre = 'Validado')
       = (SELECT count(*) FROM incidentes i JOIN reportes r USING (id_reporte)
        WHERE r.id_estado = (SELECT id_estado FROM estados WHERE nombre = 'Validado')
          AND NOT EXISTS (SELECT 1 FROM casos c WHERE c.id_incidente = i.id_incidente))

UNION ALL
-- C04: anti-join vs NOT IN vs diferencia de conjuntos (EXCEPT)
SELECT 'C04 LEFT JOIN = NOT IN = EXCEPT',
       (SELECT count(*) FROM reportes r LEFT JOIN evidencias ev USING (id_reporte)
        WHERE r.tipo_reporte = 'CONDICION' AND ev.id_evidencia IS NULL)
       = (SELECT count(*) FROM reportes WHERE tipo_reporte = 'CONDICION'
          AND id_reporte NOT IN (SELECT id_reporte FROM evidencias))
   AND (SELECT count(*) FROM (SELECT id_reporte FROM reportes WHERE tipo_reporte = 'CONDICION'
                              EXCEPT SELECT id_reporte FROM evidencias) t)
       = (SELECT count(*) FROM reportes WHERE tipo_reporte = 'CONDICION'
          AND id_reporte NOT IN (SELECT id_reporte FROM evidencias))

UNION ALL
-- C05: los antecedentes calculados por Haversine coinciden, incidente por incidente,
-- con los vínculos ANTECEDENTE guardados en reportes_relacionados
SELECT 'C05 Haversine = reportes_relacionados (por incidente)',
       (SELECT bool_and(por_haversine = guardados) FROM (
              SELECT (SELECT count(*) FROM reportes rc
                      WHERE rc.id_categoria = r.id_categoria AND rc.tipo_reporte = 'CONDICION'
                        AND rc.fecha_reporte < i.fecha_hora_hecho
                        AND rc.id_estado <> (SELECT id_estado FROM estados WHERE nombre = 'Rechazado')
                        AND 2 * 6371000 * asin(sqrt(power(sin(radians(rc.latitud - r.latitud) / 2), 2) +
                            cos(radians(r.latitud)) * cos(radians(rc.latitud)) *
                            power(sin(radians(rc.longitud - r.longitud) / 2), 2))) <= 100) AS por_haversine,
                     (SELECT count(*) FROM reportes_relacionados rr
                      WHERE rr.id_reporte = i.id_reporte AND rr.tipo_relacion = 'ANTECEDENTE') AS guardados
              FROM incidentes i JOIN reportes r USING (id_reporte)) t)

UNION ALL
-- C06: GROUP BY + HAVING vs subconsulta correlacionada por zona
SELECT 'C06 HAVING = subconsulta correlacionada',
       (SELECT count(*) FROM (
          SELECT r.id_zona FROM reportes r JOIN estados e USING (id_estado)
          WHERE r.tipo_reporte = 'CONDICION' AND e.nombre NOT IN ('Solucionado', 'Rechazado')
          GROUP BY r.id_zona HAVING count(*) >= 3) t)
       = (SELECT count(*) FROM zonas z
          WHERE (SELECT count(*) FROM reportes r JOIN estados e USING (id_estado)
                 WHERE r.id_zona = z.id_zona AND r.tipo_reporte = 'CONDICION'
                   AND e.nombre NOT IN ('Solucionado', 'Rechazado')) >= 3)

UNION ALL
-- C07: la tasa promedio de la red calculada como cociente de conteos
SELECT 'C07 promedio con avg = elegidas / resueltas',
       (SELECT avg((estado = 'ELEGIDA')::int) FROM postulaciones WHERE estado IN ('ELEGIDA', 'NO_ELEGIDA'))
       = (SELECT count(*) FILTER (WHERE estado = 'ELEGIDA')::numeric
                 / count(*) FILTER (WHERE estado IN ('ELEGIDA', 'NO_ELEGIDA')) FROM postulaciones)

UNION ALL
-- C08: reportes solucionados contados desde el estado actual = desde el historial
SELECT 'C08 solucionados (historial) = solucionados (estado actual)',
       (SELECT count(DISTINCT h.id_reporte) FROM historial_estados h JOIN estados e ON e.id_estado = h.id_estado_nuevo
          JOIN reportes r ON r.id_reporte = h.id_reporte
        WHERE e.nombre = 'Solucionado' AND r.tipo_reporte = 'CONDICION')
       = (SELECT count(*) FROM reportes r JOIN estados e USING (id_estado)
          WHERE e.nombre = 'Solucionado' AND r.tipo_reporte = 'CONDICION')

UNION ALL
-- C09: la vista no infla filas (una por caso) y sus antecedentes cuadran con la tabla
SELECT 'C09 vista: una fila por caso y antecedentes correctos',
       (SELECT count(*) FROM v_resumen_casos) = (SELECT count(*) FROM casos)
   AND (SELECT sum(num_antecedentes) FROM v_resumen_casos)
       = (SELECT count(*) FROM casos c JOIN incidentes i USING (id_incidente)
            JOIN reportes_relacionados rr ON rr.id_reporte = i.id_reporte AND rr.tipo_relacion = 'ANTECEDENTE')

UNION ALL
-- C10: el mapa público tiene todos los reportes salvo los rechazados
SELECT 'C10 mapa público = reportes no rechazados',
       (SELECT count(*) FROM v_mapa_publico)
       = (SELECT count(*) FROM reportes r JOIN estados e USING (id_estado) WHERE e.nombre <> 'Rechazado');
