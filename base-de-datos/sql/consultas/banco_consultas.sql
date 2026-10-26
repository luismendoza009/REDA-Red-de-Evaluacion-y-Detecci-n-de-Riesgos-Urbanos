-- =============================================================================
-- banco_consultas.sql — Banco de consultas de negocio de REDA (Entrega 2)
--
-- Cobertura de la rúbrica:
--   JOIN de 3+ tablas ............ C01, C02, C05, C08
--   Reunión externa "no tienen" .. C03, C04
--   GROUP BY + HAVING ............ C06, C10
--   Subconsulta / CTE ............ C05 (CTE), C07 (subconsulta), C08 (CTE)
--   Vista creada y consultada .... C09 (v_resumen_casos), C10 usa v_mapa_publico
--                                  (las vistas se crean en migraciones/008_vistas.sql)
-- Verificación a mano de cada resultado: sql/consultas/verificacion_consultas.sql
-- Ejecutar: psql -d reda -f sql/consultas/banco_consultas.sql
-- =============================================================================

-- -----------------------------------------------------------------------------
-- C01. ¿Cuál es el expediente básico de cada incidente: qué pasó, dónde, qué
--      categoría de falla, a qué entidad se le atribuye y en qué estado está?
--      (JOIN de 6 tablas; LEFT JOIN a entidad porque puede no estar identificada)
-- -----------------------------------------------------------------------------
\echo 'C01. Expediente básico de incidentes'
SELECT i.id_incidente,
       i.fecha_hora_hecho::date            AS fecha_hecho,
       c.nombre                            AS categoria,
       z.nombre                            AS zona,
       i.tipo_dano,
       i.hubo_atencion_medica,
       coalesce(ep.nombre, '(sin identificar)') AS entidad_responsable,
       e.nombre                            AS estado_reporte
FROM incidentes i
JOIN reportes   r  ON r.id_reporte   = i.id_reporte
JOIN categorias c  ON c.id_categoria = r.id_categoria
JOIN zonas      z  ON z.id_zona      = r.id_zona
JOIN estados    e  ON e.id_estado    = r.id_estado
LEFT JOIN entidades_publicas ep ON ep.id_entidad = i.id_entidad
ORDER BY i.fecha_hora_hecho;

-- -----------------------------------------------------------------------------
-- C02. ¿Cuánto se le ha facturado a cada abogado, cuántos casos recibió y cuántos
--      aceptó? (ingresos del negocio; JOIN de 3 tablas + agregación)
--      Nota: se agrupa directamente sobre remisiones, la única tabla con montos;
--      unir antes con otras tablas 1:N (testigos, antecedentes) inflaría la suma.
-- -----------------------------------------------------------------------------
\echo 'C02. Facturación por abogado'
SELECT a.id_abogado,
       u.nombre                                              AS abogado,
       count(*)                                              AS remisiones_recibidas,
       count(*) FILTER (WHERE rm.estado_remision = 'ACEPTADA')  AS aceptadas,
       count(*) FILTER (WHERE rm.estado_remision = 'RECHAZADA') AS rechazadas,
       sum(rm.valor_cobrado)                                 AS total_facturado_cop
FROM remisiones rm
JOIN abogados a ON a.id_abogado = rm.id_abogado
JOIN usuarios u ON u.id_usuario = a.id_usuario
GROUP BY a.id_abogado, u.nombre
ORDER BY total_facturado_cop DESC;

-- -----------------------------------------------------------------------------
-- C03. ¿Qué incidentes ya validados por un operador todavía NO tienen caso abierto?
--      Es la bandeja de trabajo del operador: decidir si se remiten o no.
--      (reunión externa con patrón "los que no tienen": LEFT JOIN ... IS NULL)
-- -----------------------------------------------------------------------------
\echo 'C03. Incidentes validados sin caso (bandeja del operador)'
SELECT i.id_incidente, r.id_reporte, c.nombre AS categoria, z.nombre AS zona,
       i.tipo_dano, r.fecha_reporte::date AS fecha_reporte
FROM incidentes i
JOIN reportes   r ON r.id_reporte   = i.id_reporte
JOIN estados    e ON e.id_estado    = r.id_estado
JOIN categorias c ON c.id_categoria = r.id_categoria
JOIN zonas      z ON z.id_zona      = r.id_zona
LEFT JOIN casos ca ON ca.id_incidente = i.id_incidente
WHERE ca.id_caso IS NULL
  AND e.nombre = 'Validado'
ORDER BY r.fecha_reporte;

-- -----------------------------------------------------------------------------
-- C04. ¿Qué reportes de condición no tienen NINGUNA foto de evidencia?
--      Sin foto, el reporte pierde casi todo su valor como antecedente probatorio:
--      se le pide al ciudadano que la complete. (LEFT JOIN ... IS NULL)
-- -----------------------------------------------------------------------------
\echo 'C04. Condiciones sin evidencia fotográfica'
SELECT r.id_reporte, c.nombre AS categoria, z.nombre AS zona, r.fecha_reporte::date AS fecha
FROM reportes r
JOIN categorias c ON c.id_categoria = r.id_categoria
JOIN zonas      z ON z.id_zona      = r.id_zona
LEFT JOIN evidencias ev ON ev.id_reporte = r.id_reporte
WHERE r.tipo_reporte = 'CONDICION'
  AND ev.id_evidencia IS NULL
ORDER BY r.fecha_reporte;

-- -----------------------------------------------------------------------------
-- C05. "La entidad ya sabía": para cada incidente, ¿cuántos reportes de condición
--      (no rechazados) de la misma categoría hubo a menos de 100 m ANTES del hecho, y desde hace
--      cuántos días se conocía la falla?
--      Se calcula con la fórmula de Haversine (distancia sobre la esfera terrestre,
--      radio 6 371 km), sin PostGIS. No usa la tabla reportes_relacionados: la
--      reconstruye desde las coordenadas, así sirve también para validar esa tabla.
--      (CTE + JOIN de 3 tablas)
-- -----------------------------------------------------------------------------
\echo 'C05. Antecedentes por incidente (Haversine, radio 100 m)'
WITH incidente AS (
    SELECT i.id_incidente, i.fecha_hora_hecho, r.id_categoria, r.latitud, r.longitud
    FROM incidentes i
    JOIN reportes r ON r.id_reporte = i.id_reporte
),
cercanos AS (
    SELECT inc.id_incidente, rc.id_reporte, rc.fecha_reporte,
           2 * 6371000 * asin(sqrt(
               power(sin(radians(rc.latitud - inc.latitud) / 2), 2) +
               cos(radians(inc.latitud)) * cos(radians(rc.latitud)) *
               power(sin(radians(rc.longitud - inc.longitud) / 2), 2)))  AS distancia_m
    FROM incidente inc
    JOIN reportes rc ON rc.id_categoria  = inc.id_categoria
                    AND rc.tipo_reporte  = 'CONDICION'
                    AND rc.fecha_reporte < inc.fecha_hora_hecho
    -- un reporte rechazado (duplicado, foto falsa...) no prueba que la entidad supiera
    JOIN estados ec  ON ec.id_estado = rc.id_estado AND ec.nombre <> 'Rechazado'
)
SELECT inc.id_incidente,
       c.nombre                                            AS categoria,
       count(ce.id_reporte)                                AS antecedentes,
       min(ce.fecha_reporte)::date                         AS primer_antecedente,
       (inc.fecha_hora_hecho::date - min(ce.fecha_reporte)::date) AS dias_conociendo_la_falla,
       round(min(ce.distancia_m)::numeric, 1)              AS distancia_minima_m
FROM incidente inc
JOIN categorias c ON c.id_categoria = inc.id_categoria
LEFT JOIN cercanos ce ON ce.id_incidente = inc.id_incidente AND ce.distancia_m <= 100
GROUP BY inc.id_incidente, c.nombre, inc.fecha_hora_hecho
ORDER BY antecedentes DESC, dias_conociendo_la_falla DESC NULLS LAST;

-- -----------------------------------------------------------------------------
-- C06. ¿Qué zonas acumulan 3 o más reportes de condición SIN solucionar?
--      (fallas abiertas que la ciudad conoce y no ha atendido; GROUP BY + HAVING)
-- -----------------------------------------------------------------------------
\echo 'C06. Zonas con 3 o más fallas abiertas'
SELECT z.nombre                              AS zona,
       z.tipo,
       count(*)                              AS fallas_abiertas,
       round(avg(r.nivel_gravedad), 2)       AS gravedad_percibida_promedio,
       min(r.fecha_reporte)::date            AS falla_abierta_mas_antigua
FROM reportes r
JOIN zonas   z ON z.id_zona   = r.id_zona
JOIN estados e ON e.id_estado = r.id_estado
WHERE r.tipo_reporte = 'CONDICION'
  AND e.nombre NOT IN ('Solucionado', 'Rechazado')
GROUP BY z.id_zona, z.nombre, z.tipo
HAVING count(*) >= 3
ORDER BY fallas_abiertas DESC, falla_abierta_mas_antigua;

-- -----------------------------------------------------------------------------
-- C07. ¿Qué abogados aceptan una proporción de casos mayor que el promedio de la
--      red? (para priorizarlos en nuevas remisiones; subconsulta en el HAVING)
--      Solo cuenta remisiones respondidas (ACEPTADA o RECHAZADA).
-- -----------------------------------------------------------------------------
\echo 'C07. Abogados con tasa de aceptación sobre el promedio'
SELECT a.id_abogado, u.nombre AS abogado,
       count(*)                                                        AS respondidas,
       round(avg((rm.estado_remision = 'ACEPTADA')::int) * 100, 1)     AS tasa_aceptacion_pct
FROM remisiones rm
JOIN abogados a ON a.id_abogado = rm.id_abogado
JOIN usuarios u ON u.id_usuario = a.id_usuario
WHERE rm.estado_remision IN ('ACEPTADA', 'RECHAZADA')
GROUP BY a.id_abogado, u.nombre
HAVING avg((rm.estado_remision = 'ACEPTADA')::int) >
       (SELECT avg((estado_remision = 'ACEPTADA')::int)
        FROM remisiones
        WHERE estado_remision IN ('ACEPTADA', 'RECHAZADA'))
ORDER BY tasa_aceptacion_pct DESC;

-- -----------------------------------------------------------------------------
-- C08. ¿Cuánto tarda, en promedio, una falla en solucionarse según su categoría?
--      Desde que se reporta hasta el cambio a 'Solucionado' en el historial.
--      (CTE + JOIN de 4 tablas; mide la "falla en el servicio")
-- -----------------------------------------------------------------------------
\echo 'C08. Tiempo promedio de solución por categoría'
WITH solucion AS (
    SELECT h.id_reporte, min(h.fecha_cambio) AS fecha_solucion
    FROM historial_estados h
    JOIN estados e ON e.id_estado = h.id_estado_nuevo
    WHERE e.nombre = 'Solucionado'
    GROUP BY h.id_reporte
)
SELECT c.nombre                                                          AS categoria,
       count(r.id_reporte)                                               AS reportes,
       count(s.id_reporte)                                               AS solucionados,
       round(100.0 * count(s.id_reporte) / count(r.id_reporte), 1)       AS pct_solucionado,
       round(avg(extract(epoch FROM s.fecha_solucion - r.fecha_reporte) / 86400)::numeric, 1) AS dias_promedio_solucion
FROM reportes r
JOIN categorias c ON c.id_categoria = r.id_categoria
LEFT JOIN solucion s ON s.id_reporte = r.id_reporte
WHERE r.tipo_reporte = 'CONDICION'
GROUP BY c.nombre
ORDER BY dias_promedio_solucion DESC NULLS FIRST;

-- -----------------------------------------------------------------------------
-- C09. ¿Cuáles son los casos más sólidos? Los que tienen más antecedentes y más
--      días de conocimiento previo de la falla por parte de la entidad.
--      (consulta sobre la vista v_resumen_casos; el IVC formal llega en la Entrega 3)
-- -----------------------------------------------------------------------------
\echo 'C09. Casos ordenados por solidez probatoria (vista v_resumen_casos)'
SELECT id_caso, estado_caso, categoria, zona, tipo_dano,
       num_antecedentes, dias_conocimiento_previo, num_testigos,
       num_remisiones, aceptado, total_cobrado
FROM v_resumen_casos
ORDER BY num_antecedentes DESC, dias_conocimiento_previo DESC NULLS LAST, num_testigos DESC;

-- -----------------------------------------------------------------------------
-- C10. ¿Dónde están los puntos críticos del mapa público? Celdas de ~110 m
--      (coordenadas redondeadas a 3 decimales) con 3 o más reportes.
--      (consulta sobre la vista v_mapa_publico + GROUP BY + HAVING)
-- -----------------------------------------------------------------------------
\echo 'C10. Puntos críticos del mapa (vista v_mapa_publico)'
SELECT round(latitud, 3)                         AS lat_celda,
       round(longitud, 3)                        AS lon_celda,
       string_agg(DISTINCT zona, ', ')           AS zonas,
       string_agg(DISTINCT categoria, ', ')      AS categorias,
       count(*)                                  AS reportes,
       count(*) FILTER (WHERE tipo_reporte = 'INCIDENTE') AS incidentes
FROM v_mapa_publico
GROUP BY round(latitud, 3), round(longitud, 3)
HAVING count(*) >= 3
ORDER BY reportes DESC, incidentes DESC;
