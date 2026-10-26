# REDA — Red de Evaluación y Detección de Riesgos Urbanos

Proyecto integrador de Base de Datos I y Programación 3 — Universidad Popular del Cesar, Valledupar.

REDA es un sistema de **documentación probatoria de fallas en infraestructura pública**: el ciudadano
reporta condiciones (huecos, alcantarillas, luminarias, señalización…) e incidentes (daños sufridos por
esas condiciones). El sistema vincula cada incidente con los reportes previos del mismo punto
("la entidad ya sabía") y, si el caso es viable, lo remite a un abogado.

## Integrantes
- Rafael Alfonso Rivera Restrepo
- Ivan Andres Rodriguez Rodriguez
- Luis Eliecer Mendoza Perpiñan

## Estado del proyecto
| Entrega | Contenido | Tag |
|---|---|---|
| 1 — Planteamiento y diseño | `documentacion/Entrega1_REDA.pdf`, `diagramas/reda_eer.drawio` y `.png` | `entrega-1` |
| 2 — Implementación | migraciones, semilla, consultas, índices (en curso) | `entrega-2` |
| 3 — Producto final | funciones, triggers, seguridad, respaldos | `entrega-final` |

## Estructura
```
documentacion/   documentos de cada entrega, diccionario de datos, decisiones
diagramas/       diagrama EER (.drawio editable + .png) y su especificación JSON
migraciones/     DDL numerado (001_..., 002_...) ejecutable en orden sobre base vacía
scripts/         utilidades (generar_diagrama.py, migrar.ps1, respaldos)
```

## Construir la base de datos (PostgreSQL 16+)
```powershell
$env:PGPASSWORD = "<tu clave de postgres>"     # no se guarda en ningún archivo
.\scripts\migrar.ps1 -Recrear -ConSemilla       # base vacía -> migraciones -> datos semilla
psql -U postgres -d reda -f sql\pruebas\verificar_semilla.sql            # coherencia de datos
psql -U postgres -d reda -f sql\pruebas\probar_restricciones.sql         # pruebas negativas
psql -U postgres -d reda -f sql\consultas\banco_consultas.sql            # banco de consultas
psql -U postgres -d reda -f sql\consultas\verificacion_consultas.sql     # verificación cruzada
psql -U postgres -d reda -f sql\indices\evidencia_indices.sql            # EXPLAIN antes/después
.\scripts\generar_diccionario.ps1                                         # diccionario desde el esquema
```
Usuarios de prueba: cualquiera de `sql/semilla/02_usuarios.sql`, contraseña `Reda2026*`.
Para generar de nuevo los datos semilla: `python scripts\generar_semilla.py`.

## Regenerar el diagrama
El `.drawio` y el `.png` se generan desde una sola especificación para que no se desincronicen:
```powershell
python scripts\generar_diagrama.py diagramas\especificacion_eer_v2.json diagramas\reda_eer   # Entrega 2 (actual)
```
`especificacion_eer_v1.json` conserva el diagrama de la Entrega 1. Requiere Python 3 con `matplotlib`.
El `.drawio` resultante se puede seguir editando en app.diagrams.net.
