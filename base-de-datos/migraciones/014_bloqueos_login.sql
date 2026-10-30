-- =============================================================================
-- 014_bloqueos_login.sql
-- Estado persistente de los intentos fallidos y bloqueos progresivos por correo.
-- No se referencia usuarios para responder igual ante correos existentes e inexistentes.
-- =============================================================================

CREATE TABLE bloqueos_login (
    correo                 VARCHAR(150) PRIMARY KEY,
    intentos_fallidos      INTEGER NOT NULL CHECK (intentos_fallidos BETWEEN 1 AND 15),
    bloqueado_hasta        DATE,
    bloqueado_permanente   BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT ck_bloqueos_login_estado CHECK (
        (bloqueado_permanente AND bloqueado_hasta IS NULL AND intentos_fallidos = 15)
        OR (NOT bloqueado_permanente AND intentos_fallidos < 15)
    )
);

COMMENT ON TABLE bloqueos_login IS 'Contador persistente de intentos fallidos de inicio de sesión y bloqueos progresivos.';
COMMENT ON COLUMN bloqueos_login.bloqueado_hasta IS 'Último día de un bloqueo temporal; nulo si no hay bloqueo temporal.';
