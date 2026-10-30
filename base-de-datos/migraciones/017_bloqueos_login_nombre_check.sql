-- Asigna un nombre de dominio estable a la regla de intentos fallidos.
ALTER TABLE bloqueos_login
    RENAME CONSTRAINT bloqueos_login_intentos_fallidos_check TO ck_bloqueos_login_intentos;
