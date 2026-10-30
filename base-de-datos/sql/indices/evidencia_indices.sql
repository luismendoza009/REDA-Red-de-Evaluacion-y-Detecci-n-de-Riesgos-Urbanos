-- =============================================================================
-- evidencia_indices.sql — Evidencia de índices: EXPLAIN ANALYZE antes / después
--
-- Con los ~140 reportes de la semilla PostgreSQL siempre elige Seq Scan (leer 3
-- páginas es más barato que usar un índice), así que la comparación no diría nada.
-- Por eso el experimento:
--   1. abre una transacción,
--   2. carga volumen sintético (200 000 reportes, 400 000 evidencias) ~ varios años
--      de operación en una ciudad mediana,
--   3. por cada índice: lo quita, mide, lo crea, mide,
--   4. ROLLBACK: la base queda exactamente como estaba (DDL en PostgreSQL es transaccional).
-- Ejecutar (con semilla):  psql -d reda -f sql/indices/evidencia_indices.sql
-- Resultado guardado en:   documentacion/entrega2/evidencia_indices.txt
-- =============================================================================
\timing off
BEGIN;
SELECT setseed(0.2026);

\echo '== Carga de volumen sintético (se deshace al final) =='
INSERT INTO reportes (id_usuario, id_categoria, id_zona, id_estado, tipo_reporte, descripcion,
                      latitud, longitud, fecha_reporte, nivel_gravedad)
SELECT (SELECT array_agg(id_usuario) FROM usuarios WHERE id_rol = 1)[1 + floor(random() * 45)::int],
       1 + floor(random() * 8)::int,
       1 + floor(random() * 30)::int,
       1 + floor(random() * 6)::int,
       'CONDICION',
       'Reporte sintético para la prueba de índices',
       round((10.43 + random() * 0.08)::numeric, 6),
       round((-73.29 + random() * 0.07)::numeric, 6),
       timestamptz '2023-01-01' + random() * interval '1390 days',
       1 + floor(random() * 5)::int
FROM generate_series(1, 200000);

INSERT INTO evidencias (id_reporte, ruta_imagen, tipo_mime, tamano_bytes, hash_sha256, fecha_carga)
SELECT r.id_reporte, 'sintetica/' || r.id_reporte || '_' || n || '.jpg', 'image/jpeg',
       200000 + (r.id_reporte * 7919 + n) % 4000000,
       encode(sha256(convert_to(r.id_reporte || '-' || n, 'UTF8')), 'hex'),
       r.fecha_reporte
FROM reportes r CROSS JOIN generate_series(1, 2) n
WHERE r.descripcion = 'Reporte sintético para la prueba de índices';

ANALYZE reportes;
ANALYZE evidencias;
SELECT (SELECT count(*) FROM reportes) AS reportes, (SELECT count(*) FROM evidencias) AS evidencias;

-- =============================================================================
-- EXPERIMENTO 1 — Búsqueda de antecedentes de un incidente (función estrella).
-- Pregunta: ¿qué reportes de CONDICIÓN de la misma categoría hay cerca del punto del
-- incidente demo, antes de la fecha del hecho? La caja de ±0.0009° (~100 m) se filtra
-- con el índice; la distancia exacta (Haversine) se calcula solo sobre los candidatos.
-- Índice: ix_reportes_antecedentes (id_categoria, tipo_reporte, latitud) INCLUDE (longitud, fecha_reporte)
-- =============================================================================
\echo ''
\echo '== EXPERIMENTO 1 · ANTES (sin ix_reportes_antecedentes; solo existe el índice simple de la FK id_categoria) =='
DROP INDEX ix_reportes_antecedentes;

EXPLAIN (ANALYZE, BUFFERS, COSTS OFF, SUMMARY ON)
SELECT rc.id_reporte, rc.fecha_reporte
FROM reportes rc, (SELECT r.id_categoria, r.latitud, r.longitud, i.fecha_hecho
                   FROM incidentes i JOIN reportes r USING (id_reporte)
                   WHERE i.id_incidente = 18) inc
WHERE rc.id_categoria = inc.id_categoria
  AND rc.tipo_reporte = 'CONDICION'
  AND rc.latitud  BETWEEN inc.latitud  - 0.0009 AND inc.latitud  + 0.0009
  AND rc.longitud BETWEEN inc.longitud - 0.0009 AND inc.longitud + 0.0009
  AND rc.fecha_reporte < inc.fecha_hecho;

\echo '== EXPERIMENTO 1 · DESPUÉS (con ix_reportes_antecedentes) =='
CREATE INDEX ix_reportes_antecedentes ON reportes (id_categoria, tipo_reporte, latitud) INCLUDE (longitud, fecha_reporte);
ANALYZE reportes;

EXPLAIN (ANALYZE, BUFFERS, COSTS OFF, SUMMARY ON)
SELECT rc.id_reporte, rc.fecha_reporte
FROM reportes rc, (SELECT r.id_categoria, r.latitud, r.longitud, i.fecha_hecho
                   FROM incidentes i JOIN reportes r USING (id_reporte)
                   WHERE i.id_incidente = 18) inc
WHERE rc.id_categoria = inc.id_categoria
  AND rc.tipo_reporte = 'CONDICION'
  AND rc.latitud  BETWEEN inc.latitud  - 0.0009 AND inc.latitud  + 0.0009
  AND rc.longitud BETWEEN inc.longitud - 0.0009 AND inc.longitud + 0.0009
  AND rc.fecha_reporte < inc.fecha_hecho;

-- =============================================================================
-- EXPERIMENTO 2 — Índice de clave foránea evidencias(id_reporte).
-- Pregunta: ¿cuáles son las fotos del expediente de un caso? (JOIN casos ->
-- incidentes -> reportes -> evidencias). PostgreSQL no indexa las FK por su cuenta.
-- =============================================================================
\echo ''
\echo '== EXPERIMENTO 2 · ANTES (sin ix_evidencias_reporte) =='
DROP INDEX ix_evidencias_reporte;

EXPLAIN (ANALYZE, BUFFERS, COSTS OFF, SUMMARY ON)
SELECT ca.id_caso, ev.ruta_imagen, ev.hash_sha256, ev.fecha_captura
FROM casos ca
JOIN incidentes i  ON i.id_incidente = ca.id_incidente
JOIN evidencias ev ON ev.id_reporte  = i.id_reporte
WHERE ca.id_caso = 8;

\echo '== EXPERIMENTO 2 · DESPUÉS (con ix_evidencias_reporte) =='
CREATE INDEX ix_evidencias_reporte ON evidencias (id_reporte);
ANALYZE evidencias;

EXPLAIN (ANALYZE, BUFFERS, COSTS OFF, SUMMARY ON)
SELECT ca.id_caso, ev.ruta_imagen, ev.hash_sha256, ev.fecha_captura
FROM casos ca
JOIN incidentes i  ON i.id_incidente = ca.id_incidente
JOIN evidencias ev ON ev.id_reporte  = i.id_reporte
WHERE ca.id_caso = 8;

ROLLBACK;
\echo ''
\echo '== ROLLBACK: volumen sintético eliminado; índices originales intactos =='
SELECT count(*) AS reportes_tras_rollback FROM reportes;
