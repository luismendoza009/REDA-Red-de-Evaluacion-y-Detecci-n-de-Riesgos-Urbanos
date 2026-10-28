-- =============================================================================
-- 012_ubicacion_zonas.sql
-- Cada zona guarda un punto de referencia real dentro del barrio (tomado de OpenStreetMap)
-- y la fuente de donde salió. Sirve para:
--   1. Sugerir el barrio automáticamente según el punto que el ciudadano marca en el mapa
--      (antes lo elegía a mano y podía quedar un reporte de la UPC marcado como "La Nevada").
--   2. Que los datos de prueba caigan dentro de su barrio real.
-- Las columnas admiten NULL: una zona nueva puede crearse antes de ubicarla.
-- Latitud y longitud van juntas (ambas o ninguna) y dentro del rango geográfico válido.
-- =============================================================================

ALTER TABLE zonas
    ADD COLUMN latitud_referencia  NUMERIC(9, 6),
    ADD COLUMN longitud_referencia NUMERIC(9, 6),
    ADD COLUMN fuente_ubicacion    VARCHAR(200),
    ADD CONSTRAINT ck_zonas_ubicacion_completa
        CHECK ((latitud_referencia IS NULL) = (longitud_referencia IS NULL)),
    ADD CONSTRAINT ck_zonas_latitud  CHECK (latitud_referencia  BETWEEN -90  AND 90),
    ADD CONSTRAINT ck_zonas_longitud CHECK (longitud_referencia BETWEEN -180 AND 180),
    ADD CONSTRAINT ck_zonas_fuente
        CHECK (fuente_ubicacion IS NULL OR latitud_referencia IS NOT NULL);

COMMENT ON COLUMN zonas.latitud_referencia IS 'Latitud de un punto de referencia dentro del barrio (OpenStreetMap). No es el centroide exacto.';
COMMENT ON COLUMN zonas.longitud_referencia IS 'Longitud del punto de referencia del barrio (OpenStreetMap).';
COMMENT ON COLUMN zonas.fuente_ubicacion IS 'De dónde sale el punto de referencia (lugar de OpenStreetMap y fecha de consulta).';
