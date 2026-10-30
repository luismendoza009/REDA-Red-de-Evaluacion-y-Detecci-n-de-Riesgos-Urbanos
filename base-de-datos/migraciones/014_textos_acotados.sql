-- =============================================================================
-- 014_textos_acotados.sql
-- Los textos libres quedan acotados. Las columnas siguen siendo TEXT (las entidades JPA las
-- declaran así y ddl-auto=validate lo comprueba), pero cada una tiene un tope con nombre propio.
-- Los topes son los mismos que valida la API (@Size en programacion/backend/.../peticion), así
-- que la base y la aplicación dicen lo mismo: un texto más largo es un error del cliente o
-- alguien escribiendo directo en la base, y en los dos casos la base lo rechaza.
-- =============================================================================

ALTER TABLE categorias
    ADD CONSTRAINT ck_categorias_descripcion CHECK (length(descripcion) <= 300);

ALTER TABLE abogados
    ADD CONSTRAINT ck_abogados_presentacion CHECK (length(presentacion) <= 600);

ALTER TABLE reportes
    ADD CONSTRAINT ck_reportes_descripcion_max CHECK (length(descripcion) <= 2000);

ALTER TABLE historial_estados
    ADD CONSTRAINT ck_historial_observacion CHECK (length(observacion) <= 1000);

ALTER TABLE incidentes
    ADD CONSTRAINT ck_incidentes_descripcion_max CHECK (length(descripcion_dano) <= 2000);

ALTER TABLE casos
    ADD CONSTRAINT ck_casos_observacion CHECK (length(observacion) <= 1000);

ALTER TABLE remisiones
    ADD CONSTRAINT ck_remisiones_motivo_max CHECK (length(motivo_rechazo) <= 1000);

ALTER TABLE postulaciones
    ADD CONSTRAINT ck_postulaciones_mensaje_max CHECK (length(mensaje) <= 1500);

ALTER TABLE versiones_indice
    ADD CONSTRAINT ck_versiones_descripcion CHECK (length(descripcion) BETWEEN 10 AND 500);

ALTER TABLE parametros_indice
    ADD CONSTRAINT ck_parametros_descripcion CHECK (length(descripcion) BETWEEN 10 AND 300);
