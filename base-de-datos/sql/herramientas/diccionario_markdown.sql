-- =============================================================================
-- diccionario_markdown.sql
-- Genera el diccionario de datos en Markdown leyendo el catálogo de PostgreSQL
-- (information_schema + COMMENT ON de las migraciones). Así el diccionario siempre
-- describe el esquema REAL y no una versión vieja escrita a mano.
-- Uso: .\scripts\generar_diccionario.ps1   (escribe documentacion/diccionario_datos.md)
-- =============================================================================
WITH tablas AS (
    SELECT c.oid, c.relname AS tabla, obj_description(c.oid, 'pg_class') AS descripcion,
           -- orden de presentación = orden de creación en las migraciones
           c.oid::int AS orden
    FROM pg_class c
    JOIN pg_namespace n ON n.oid = c.relnamespace
    WHERE n.nspname = 'public' AND c.relkind = 'r' AND c.relname <> 'control_migraciones'
),
columnas AS (
    SELECT t.tabla, col.ordinal_position,
           col.column_name,
           CASE
             WHEN col.data_type = 'character varying' THEN 'VARCHAR(' || col.character_maximum_length || ')'
             WHEN col.data_type = 'character'         THEN 'CHAR(' || col.character_maximum_length || ')'
             WHEN col.data_type = 'numeric'           THEN 'NUMERIC(' || col.numeric_precision || ',' || col.numeric_scale || ')'
             WHEN col.data_type = 'timestamp with time zone' THEN 'TIMESTAMPTZ'
             ELSE upper(col.data_type)
           END AS tipo,
           CASE WHEN col.is_nullable = 'YES' THEN 'Sí' ELSE 'No' END AS nulo,
           (SELECT string_agg(DISTINCT CASE tc.constraint_type WHEN 'PRIMARY KEY' THEN 'PK' WHEN 'FOREIGN KEY' THEN 'FK'
                                                                WHEN 'UNIQUE' THEN 'UQ' END, ', ')
            FROM information_schema.key_column_usage k
            JOIN information_schema.table_constraints tc
              ON tc.constraint_name = k.constraint_name AND tc.table_name = k.table_name
            WHERE k.table_name = t.tabla AND k.column_name = col.column_name) AS claves,
           CASE WHEN col.is_identity = 'YES' THEN 'identidad'
                ELSE regexp_replace(coalesce(col.column_default, ''), '::[a-z ]+', '', 'g') END AS defecto,
           col_description(t.oid, col.ordinal_position::int) AS significado
    FROM tablas t
    JOIN information_schema.columns col ON col.table_schema = 'public' AND col.table_name = t.tabla
),
restricciones AS (
    SELECT t.tabla,
           string_agg('- `' || con.conname || '`: `' || pg_get_constraintdef(con.oid) || '`', E'\n' ORDER BY con.contype, con.conname) AS lista
    FROM tablas t
    JOIN pg_constraint con ON con.conrelid = t.oid
    WHERE con.contype IN ('c', 'f', 'u')
    GROUP BY t.tabla
),
indices AS (
    SELECT t.tabla,
           string_agg('- `' || i.indexname || '`: `' || regexp_replace(i.indexdef, '^.* USING ', '') || '`', E'\n' ORDER BY i.indexname) AS lista
    FROM tablas t
    JOIN pg_indexes i ON i.schemaname = 'public' AND i.tablename = t.tabla
    -- se listan solo los índices que no nacen de una PK/UNIQUE (esos ya están en restricciones)
    WHERE NOT EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conname = i.indexname)
    GROUP BY t.tabla
)
SELECT string_agg(bloque, E'\n' ORDER BY orden)
FROM (
    SELECT t.orden,
           '## ' || t.tabla || E'\n\n' || coalesce(t.descripcion, '') || E'\n\n' ||
           E'| Columna | Tipo | Nulo | Claves | Defecto | Significado de negocio |\n|---|---|---|---|---|---|\n' ||
           (SELECT string_agg(format('| %s | %s | %s | %s | %s | %s |', c.column_name, c.tipo, c.nulo,
                                     coalesce(c.claves, ''), c.defecto, replace(coalesce(c.significado, ''), '|', '/')),
                              E'\n' ORDER BY c.ordinal_position)
            FROM columnas c WHERE c.tabla = t.tabla) ||
           E'\n\n**Restricciones:**\n\n' || coalesce(r.lista, '- (solo clave primaria)') ||
           coalesce(E'\n\n**Índices adicionales:**\n\n' || ix.lista, '') || E'\n'
           AS bloque
    FROM tablas t
    LEFT JOIN restricciones r ON r.tabla = t.tabla
    LEFT JOIN indices ix ON ix.tabla = t.tabla
) b;
