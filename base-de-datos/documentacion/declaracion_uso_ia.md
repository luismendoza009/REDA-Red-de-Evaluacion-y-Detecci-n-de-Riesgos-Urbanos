# Declaración de uso de inteligencia artificial — BORRADOR

> Borrador para completar y firmar por el grupo. La política del curso exige declarar **para qué** se
> usó IA, **en qué partes** y **cómo se verificó**. Cada integrante debe poder explicar cualquier línea.

**Herramienta:** Claude (Anthropic), mediante Claude Code en VS Code.

## Base de Datos I

| Parte | Qué hizo la IA | Cómo se verificó |
|---|---|---|
| Migraciones 001–011 | Redactó el DDL a partir del esquema de la Entrega 1 y de las decisiones del grupo (enfoque probatorio, elección del abogado por el ciudadano, calificaciones) | Se ejecutan en orden sobre una base vacía de PostgreSQL 18 (`scripts/migrar.ps1 -Recrear`). 27 pruebas negativas (`sql/pruebas/probar_restricciones.sql`) comprueban que cada restricción clave rechaza datos inválidos y que la detiene la restricción esperada |
| `scripts/migrar.ps1` | Lo escribió | Se ejecutó sobre base vacía y se volvió a ejecutar para comprobar que no reaplica migraciones |
| Generador y datos semilla | Escribió el generador en Python | 15 comprobaciones de coherencia (`sql/pruebas/verificar_semilla.sql`), todas en 0 |
| Banco de consultas | Redactó las 10 consultas | Cada resultado se recalculó por otro camino SQL (`sql/consultas/verificacion_consultas.sql`). Las cifras de C02 y C07 se comprobaron a mano |
| Evidencia de índices | Diseñó el experimento y escribió el script | Los planes de ejecución reales quedaron en `evidencia_indices.txt` |
| Auditoría de normalización, correcciones, decisiones | Redactó los borradores | **Pendiente: revisión del grupo** |
| Diagrama EER | Escribió el generador y la especificación de la disposición | Revisión visual del PNG; el `.drawio` se abre en app.diagrams.net |

## Programación 3

| Parte | Qué hizo la IA | Cómo se verificó |
|---|---|---|
| Backend Spring Boot (capas, dominio, seguridad, API) | Escribió el código | 71 pruebas unitarias y 14 de integración contra PostgreSQL (`mvn verify -Pintegracion`). `ddl-auto=validate` comprueba que las entidades coinciden con las migraciones |
| Auditorías GRASP y SOLID | Revisó su propio código, encontró los hallazgos y aplicó las correcciones | Las pruebas pasan igual antes y después de la refactorización; el código anterior quedó en `programacion/documentacion/auditorias/antes/` |
| Interfaz web (mapa, formularios, vistas por rol) | Escribió HTML, CSS y JavaScript | Capturas automáticas con Edge en modo escritorio y celular (390 px), con sesiones de cada rol |
| Paleta de colores | Aplicó una paleta validada | Validador de contraste y daltonismo sobre el fondo oscuro |

## Errores que la verificación encontró (y se corrigieron)

- El cambio de estado guardaba el historial sin su reporte (`save` hacía `merge`): lo detectó la prueba de punta a punta.
- Un reporte rechazado contaba como antecedente: se vio en la prueba del flujo de la demo.
- Al subir una foto, revisar el hash antes que el permiso revelaba que la foto existía.
- Una prueba de privacidad daba falso positivo con "pavimento fracturado": se cambió a comparación exacta.

## Lo que la IA NO decidió

- El cambio de enfoque del negocio, que el ciudadano elija a su abogado, las calificaciones y que las fotos sean privadas (decisiones del grupo).
- Los supuestos marcados en los `decisiones.md`, que quedan para validar con el grupo.

## Pendiente de completar por el grupo

- [ ] Qué integrante revisó cada parte.
- [ ] Partes reescritas o corregidas a mano.
- [ ] Firma o nombre de cada integrante.
