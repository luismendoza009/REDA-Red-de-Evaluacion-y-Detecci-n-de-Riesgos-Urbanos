# Declaración de uso de inteligencia artificial — BORRADOR

> Borrador para completar y firmar por el grupo. La política del curso exige declarar **para qué** se
> usó IA, **en qué partes** y **cómo se verificó**. Cada integrante debe poder explicar cualquier línea.

**Herramienta:** Claude (Anthropic), mediante Claude Code en VS Code.

## Entrega 2

| Parte | Qué hizo la IA | Cómo se verificó |
|---|---|---|
| Migraciones 001–008 | Redactó el DDL a partir del esquema de la Entrega 1 y del modelo extendido acordado por el grupo | Se ejecutaron en orden sobre una base vacía de PostgreSQL 18 (`scripts/migrar.ps1 -Recrear`). 19 pruebas negativas (`sql/pruebas/probar_restricciones.sql`) comprueban que cada restricción clave rechaza datos inválidos, y que la detiene la restricción esperada |
| `scripts/migrar.ps1` | Lo escribió | Se ejecutó sobre base vacía y se volvió a ejecutar para comprobar que no reaplica migraciones |
| Generador y datos semilla | Escribió el generador en Python | 11 comprobaciones de coherencia (`sql/pruebas/verificar_semilla.sql`), todas en 0 |
| Banco de consultas | Redactó las 10 consultas | Cada resultado se recalculó por otro camino SQL (`sql/consultas/verificacion_consultas.sql`). Las cifras de C02 y C07 se comprobaron a mano |
| Evidencia de índices | Diseñó el experimento y escribió el script | Los planes de ejecución reales quedaron en `evidencia_indices.txt` |
| Auditoría de normalización, correcciones, decisiones | Redactó los borradores | **Pendiente: revisión del grupo** |
| Diagrama EER | Escribió el generador y la especificación de la disposición | Revisión visual del PNG; el `.drawio` se abre en app.diagrams.net |

## Lo que la IA NO decidió

- El cambio de enfoque del negocio (decisión del grupo).
- Los supuestos marcados en `documentacion/decisiones.md`, que quedan para validar con el grupo.

## Pendiente de completar por el grupo

- [ ] Qué integrante revisó cada parte.
- [ ] Partes reescritas o corregidas a mano.
- [ ] Firma o nombre de cada integrante.
