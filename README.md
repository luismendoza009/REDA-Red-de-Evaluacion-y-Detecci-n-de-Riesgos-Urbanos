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

## Organización del repositorio

El repositorio está separado por asignatura:

```
base-de-datos/            BASE DE DATOS I
├─ migraciones/           DDL numerado (001_..., 002_...) ejecutable en orden sobre base vacía
├─ sql/
│   ├─ semilla/           datos simulados de Valledupar
│   ├─ consultas/         banco de consultas + verificación cruzada
│   ├─ indices/           evidencia EXPLAIN ANALYZE antes/después
│   ├─ pruebas/           coherencia de la semilla y pruebas negativas de restricciones
│   └─ herramientas/      generador del diccionario
├─ scripts/               migrar.ps1, generadores (semilla, diccionario, diagrama, documento)
├─ diagramas/             diagrama EER (.drawio editable + .png) y su especificación JSON
└─ documentacion/         entregas (Entrega1, entrega2/), diccionario, decisiones, datos simulados, uso de IA

programacion/             PROGRAMACIÓN 3
├─ backend/               API REST en Java 17 + Spring Boot (arquitectura en capas)
└─ documentacion/         decisiones de diseño, auditorías GRASP y SOLID
```

## Estado
| Entrega (BD) | Contenido | Tag |
|---|---|---|
| 1 — Planteamiento y diseño | `base-de-datos/documentacion/Entrega1_REDA.pdf`, diagrama EER | `entrega-1` |
| 2 — Implementación | migraciones, semilla, consultas, índices, normalización | `entrega-2` (pendiente) |
| 3 — Producto final | funciones, triggers, seguridad, respaldos | `entrega-final` |

## Base de datos (PostgreSQL 16+)
Desde la carpeta `base-de-datos`:
```powershell
cd base-de-datos
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

Diagrama (el `.drawio` y el `.png` salen de una sola especificación para que no se desincronicen):
```powershell
python scripts\generar_diagrama.py diagramas\especificacion_eer_v2.json diagramas\reda_eer
```
`especificacion_eer_v1.json` conserva el diagrama de la Entrega 1. Requiere Python 3 con `matplotlib`.

## Backend (Java 17 + Maven)
Requiere la base creada con los pasos anteriores. Desde `programacion/backend`:
```powershell
cd programacion\backend
$env:REDA_DB_CLAVE    = "<tu clave de postgres>"
$env:REDA_JWT_SECRETO = "<texto aleatorio de 32+ caracteres>"
mvn spring-boot:run
```
Documentación interactiva de la API: http://localhost:8080/swagger-ui.html
Alternativa a las variables de entorno: copiar `application-example.properties` como
`application-local.properties` (está en `.gitignore`).
