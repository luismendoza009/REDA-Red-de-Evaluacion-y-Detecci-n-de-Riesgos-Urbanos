-- DATOS SIMULADOS generados por scripts/generar_semilla.py (no editar a mano).
-- Ninguna persona es real. Ver documentacion/datos_simulados.md

INSERT INTO bloqueos_login (correo, intentos_fallidos, bloqueado_hasta, bloqueado_permanente) VALUES
  ('prueba.bloqueo.nuevo@reda.test', 1, NULL, false),
  ('prueba.bloqueo.primero@reda.test', 5, now() + interval '3 minutes', false),
  ('prueba.bloqueo.segundo@reda.test', 10, now() + interval '10 minutes', false),
  ('prueba.bloqueo.casi@reda.test', 14, now() + interval '10 minutes', false),
  ('prueba.bloqueo.permanente@reda.test', 15, NULL, true);
