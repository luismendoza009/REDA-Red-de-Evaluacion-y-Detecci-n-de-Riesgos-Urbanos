-- =============================================================================
-- 008_vistas.sql
-- Vistas de negocio.
-- =============================================================================

-- Mapa público: lo que cualquier visitante puede ver. NO incluye autor, correo ni datos
-- del daño (que son personales o sensibles). Las coordenadas se redondean a 4 decimales
-- (~11 m) para no exponer la ubicación exacta de una vivienda.
CREATE VIEW v_mapa_publico AS
SELECT r.id_reporte,
       r.tipo_reporte,
       c.nombre                     AS categoria,
       z.nombre                     AS zona,
       e.nombre                     AS estado,
       round(r.latitud, 4)          AS latitud,
       round(r.longitud, 4)         AS longitud,
       r.fecha_reporte::date        AS fecha,
       r.nivel_gravedad,
       (SELECT count(*) FROM evidencias ev WHERE ev.id_reporte = r.id_reporte) AS num_evidencias
FROM reportes r
JOIN categorias c ON c.id_categoria = r.id_categoria
JOIN zonas      z ON z.id_zona      = r.id_zona
JOIN estados    e ON e.id_estado    = r.id_estado
WHERE e.nombre <> 'Rechazado';

COMMENT ON VIEW v_mapa_publico IS 'Reportes visibles en el mapa público, sin datos personales ni sensibles; excluye rechazados.';

-- Resumen de cada caso para el panel del operador y del abogado.
-- Se agregan los antecedentes y las remisiones en subconsultas ANTES de unir, para que
-- un caso con 3 antecedentes y 2 remisiones no aparezca 6 veces (totales inflados).
CREATE VIEW v_resumen_casos AS
WITH antecedentes AS (
    SELECT rr.id_reporte,
           count(*)               AS num_antecedentes,
           min(ra.fecha_reporte)  AS primer_antecedente
    FROM reportes_relacionados rr
    JOIN reportes ra ON ra.id_reporte = rr.id_reporte_relacionado
    WHERE rr.tipo_relacion = 'ANTECEDENTE'
    GROUP BY rr.id_reporte
),
remis AS (
    SELECT id_caso,
           count(*)                                        AS num_remisiones,
           count(*) FILTER (WHERE estado_remision = 'ACEPTADA') AS aceptadas,
           sum(valor_cobrado)                              AS total_cobrado
    FROM remisiones
    GROUP BY id_caso
)
SELECT ca.id_caso,
       ec.nombre                              AS estado_caso,
       r.id_reporte,
       c.nombre                               AS categoria,
       z.nombre                               AS zona,
       i.fecha_hora_hecho,
       i.tipo_dano,
       i.hubo_atencion_medica,
       ep.nombre                              AS entidad_responsable,
       coalesce(a.num_antecedentes, 0)        AS num_antecedentes,
       a.primer_antecedente,
       CASE WHEN a.primer_antecedente IS NOT NULL
            THEN (i.fecha_hora_hecho::date - a.primer_antecedente::date) END AS dias_conocimiento_previo,
       (SELECT count(*) FROM testigos t WHERE t.id_incidente = i.id_incidente) AS num_testigos,
       coalesce(rm.num_remisiones, 0)         AS num_remisiones,
       coalesce(rm.aceptadas, 0) > 0          AS aceptado,
       coalesce(rm.total_cobrado, 0)          AS total_cobrado,
       ca.fecha_creacion
FROM casos ca
JOIN estados_caso ec        ON ec.id_estado_caso = ca.id_estado_caso
JOIN incidentes i           ON i.id_incidente    = ca.id_incidente
JOIN reportes r             ON r.id_reporte      = i.id_reporte
JOIN categorias c           ON c.id_categoria    = r.id_categoria
JOIN zonas z                ON z.id_zona         = r.id_zona
LEFT JOIN entidades_publicas ep ON ep.id_entidad = i.id_entidad
LEFT JOIN antecedentes a    ON a.id_reporte      = r.id_reporte
LEFT JOIN remis rm          ON rm.id_caso        = ca.id_caso;

COMMENT ON VIEW v_resumen_casos IS 'Un renglón por caso: incidente, entidad, antecedentes ("la entidad ya sabía desde..."), testigos y remisiones.';
