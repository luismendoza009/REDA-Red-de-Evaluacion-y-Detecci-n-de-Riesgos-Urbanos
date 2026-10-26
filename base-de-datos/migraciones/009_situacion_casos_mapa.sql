-- =============================================================================
-- 009_situacion_casos_mapa.sql
-- El mapa público necesita mostrar en qué SITUACIÓN está cada reporte, en el lenguaje
-- del ciudadano (no en el de los catálogos internos):
--   CON_ABOGADO : es un incidente cuyo caso aceptó un abogado y no se ha cerrado.
--   CERRADO     : el caso se cerró o lo rechazaron todos los abogados, o la falla se solucionó.
--   ABIERTO     : todo lo demás (pendiente de revisión, validado, remitido sin respuesta...).
-- No se edita la migración 008 (ya aplicada): se reemplaza la vista con CREATE OR REPLACE,
-- que solo permite AGREGAR columnas al final, así que las columnas existentes no cambian.
-- =============================================================================

CREATE OR REPLACE VIEW v_mapa_publico AS
SELECT r.id_reporte,
       r.tipo_reporte,
       c.nombre                     AS categoria,
       z.nombre                     AS zona,
       e.nombre                     AS estado,
       round(r.latitud, 4)          AS latitud,
       round(r.longitud, 4)         AS longitud,
       r.fecha_reporte::date        AS fecha,
       r.nivel_gravedad,
       (SELECT count(*) FROM evidencias ev WHERE ev.id_reporte = r.id_reporte) AS num_evidencias,
       -- columnas nuevas (al final, como exige CREATE OR REPLACE VIEW)
       r.descripcion,
       CASE
           WHEN ec.nombre IN ('Cerrado', 'Rechazado') OR e.nombre = 'Solucionado' THEN 'CERRADO'
           WHEN EXISTS (SELECT 1 FROM remisiones rm
                        WHERE rm.id_caso = ca.id_caso AND rm.estado_remision = 'ACEPTADA') THEN 'CON_ABOGADO'
           ELSE 'ABIERTO'
       END                          AS situacion,
       ec.nombre                    AS estado_caso
FROM reportes r
JOIN categorias c ON c.id_categoria = r.id_categoria
JOIN zonas      z ON z.id_zona      = r.id_zona
JOIN estados    e ON e.id_estado    = r.id_estado
LEFT JOIN incidentes   i  ON i.id_reporte      = r.id_reporte
LEFT JOIN casos        ca ON ca.id_incidente   = i.id_incidente
LEFT JOIN estados_caso ec ON ec.id_estado_caso = ca.id_estado_caso
WHERE e.nombre <> 'Rechazado';

COMMENT ON VIEW v_mapa_publico IS 'Reportes visibles en el mapa público, sin datos personales ni del daño; excluye rechazados. Incluye la situación pública del caso (ABIERTO, CON_ABOGADO, CERRADO).';
