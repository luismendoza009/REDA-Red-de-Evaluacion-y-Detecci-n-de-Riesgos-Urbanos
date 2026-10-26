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

## Regenerar el diagrama
El `.drawio` y el `.png` se generan desde una sola especificación para que no se desincronicen:
```powershell
python scripts\generar_diagrama.py diagramas\especificacion_eer_v1.json diagramas\reda_eer
```
Requiere Python 3 con `matplotlib`. El `.drawio` resultante se puede seguir editando en app.diagrams.net.
