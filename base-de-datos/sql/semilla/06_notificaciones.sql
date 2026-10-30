-- DATOS SIMULADOS generados por scripts/generar_semilla.py (no editar a mano).
-- Ninguna persona es real. Ver documentacion/datos_simulados.md

-- Cinco avisos para autores distintos; incluye estados leídos y pendientes.
WITH candidatas AS (
    SELECT h.id_historial,
           r.id_usuario,
           h.fecha_cambio,
           row_number() OVER (PARTITION BY r.id_usuario ORDER BY h.fecha_cambio DESC, h.id_historial DESC) AS por_usuario
    FROM historial_estados h
    JOIN reportes r USING (id_reporte)
),
seleccionadas AS (
    SELECT id_historial, id_usuario, fecha_cambio
    FROM candidatas
    WHERE por_usuario = 1
    ORDER BY id_usuario
    LIMIT 5
),
numeradas AS (
    SELECT id_historial, id_usuario, fecha_cambio,
           row_number() OVER (ORDER BY id_usuario) AS numero
    FROM seleccionadas
)
INSERT INTO notificaciones (id_usuario, id_historial, fecha_creacion, fecha_lectura)
SELECT id_usuario,
       id_historial,
       fecha_cambio,
       CASE WHEN numero <= 2 THEN NULL ELSE fecha_cambio + interval '1 minute' END
FROM numeradas;
