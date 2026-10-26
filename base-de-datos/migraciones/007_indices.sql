-- =============================================================================
-- 007_indices.sql
-- Índices de rendimiento.
-- PostgreSQL crea índice automáticamente para PRIMARY KEY y UNIQUE, pero NO para
-- las claves foráneas. Sin índice en la FK, cada JOIN por esa columna y cada borrado
-- en la tabla padre (que debe revisar RESTRICT/CASCADE) recorre la tabla hija completa.
-- Se omiten las FK que ya quedan cubiertas como primera columna de una PK/UNIQUE:
--   abogados.id_usuario, incidentes.id_reporte, casos.id_incidente (UNIQUE),
--   reportes_relacionados.id_reporte (PK), parametros_indice.id_version (PK),
--   testigos.id_incidente (UNIQUE id_incidente, nombre),
--   remisiones.id_caso (UNIQUE id_caso, id_abogado).
-- Evidencia antes/después: sql/indices/evidencia_indices.sql
-- =============================================================================

-- FK de joins frecuentes
CREATE INDEX ix_usuarios_rol            ON usuarios (id_rol);
CREATE INDEX ix_reportes_usuario        ON reportes (id_usuario);
CREATE INDEX ix_reportes_categoria      ON reportes (id_categoria);
CREATE INDEX ix_reportes_zona           ON reportes (id_zona);
CREATE INDEX ix_reportes_estado         ON reportes (id_estado);
CREATE INDEX ix_evidencias_reporte      ON evidencias (id_reporte);
CREATE INDEX ix_evaluaciones_reporte    ON evaluaciones (id_reporte);
CREATE INDEX ix_evaluaciones_usuario    ON evaluaciones (id_usuario);
CREATE INDEX ix_historial_reporte       ON historial_estados (id_reporte, fecha_cambio);
CREATE INDEX ix_historial_anterior      ON historial_estados (id_estado_anterior);
CREATE INDEX ix_historial_nuevo         ON historial_estados (id_estado_nuevo);
CREATE INDEX ix_historial_usuario       ON historial_estados (id_usuario);
CREATE INDEX ix_relacionados_relacionado ON reportes_relacionados (id_reporte_relacionado);
CREATE INDEX ix_incidentes_entidad      ON incidentes (id_entidad);
CREATE INDEX ix_casos_estado            ON casos (id_estado_caso);
CREATE INDEX ix_casos_operador          ON casos (id_operador);
CREATE INDEX ix_remisiones_abogado      ON remisiones (id_abogado);
CREATE INDEX ix_calculos_reporte        ON calculos_indice (id_reporte, fecha_calculo DESC);
CREATE INDEX ix_calculos_version        ON calculos_indice (id_version);

-- Búsqueda de antecedentes: "reportes de CONDICIÓN de la misma categoría, cerca de un
-- punto y anteriores a una fecha". La consulta filtra por igualdad en categoría y tipo,
-- y por rango en latitud (caja que rodea el radio de búsqueda). Las columnas de igualdad
-- van primero y la de rango al final, que es el orden en que un B-tree las aprovecha.
CREATE INDEX ix_reportes_antecedentes   ON reportes (id_categoria, tipo_reporte, latitud)
    INCLUDE (longitud, fecha_reporte);
