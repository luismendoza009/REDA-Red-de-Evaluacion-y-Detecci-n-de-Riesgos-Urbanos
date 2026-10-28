-- =============================================================================
-- 013_foto_perfil.sql
-- Foto de perfil de los usuarios (ciudadanos, abogados y personal).
-- El archivo va en disco, como las evidencias; en la base solo la ruta generada y el formato.
-- La foto va en usuarios (y no en abogados) porque todos los roles pueden tener una.
-- Que un abogado solo pueda armar su perfil público cuando está verificado es una regla
-- de la aplicación (depende de abogados.activo, otra tabla); la base garantiza el resto.
-- =============================================================================

ALTER TABLE usuarios
    ADD COLUMN foto_perfil       VARCHAR(255),
    ADD COLUMN foto_perfil_mime  VARCHAR(20),
    ADD COLUMN fecha_foto_perfil TIMESTAMPTZ,
    -- los tres datos van juntos: o hay foto completa o no hay foto
    ADD CONSTRAINT ck_usuarios_foto_completa CHECK (
        (foto_perfil IS NULL AND foto_perfil_mime IS NULL AND fecha_foto_perfil IS NULL)
        OR (foto_perfil IS NOT NULL AND foto_perfil_mime IS NOT NULL AND fecha_foto_perfil IS NOT NULL)),
    -- mismos formatos que las evidencias (ck_evidencias_mime)
    ADD CONSTRAINT ck_usuarios_foto_mime CHECK (foto_perfil_mime IN ('image/jpeg', 'image/png', 'image/webp')),
    ADD CONSTRAINT uq_usuarios_foto UNIQUE (foto_perfil);

COMMENT ON COLUMN usuarios.foto_perfil IS '[PERSONAL] Ruta relativa (nombre generado) de la foto de perfil. Solo la ven el propio usuario y el personal; la de un abogado verificado, cualquier usuario con sesión.';
COMMENT ON COLUMN usuarios.foto_perfil_mime IS 'Formato real de la foto de perfil (detectado por sus bytes): image/jpeg, image/png o image/webp.';
COMMENT ON COLUMN usuarios.fecha_foto_perfil IS 'Cuándo se cargó la foto de perfil actual.';
