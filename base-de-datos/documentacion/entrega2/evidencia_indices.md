# Índices con evidencia — REDA (Entrega 2)

Script: `sql/indices/evidencia_indices.sql` · Salida completa: `documentacion/entrega2/evidencia_indices.txt`
Motor: PostgreSQL 18.6, equipo del grupo (Windows 11).

## Método

Con los ~140 reportes de la semilla, PostgreSQL elige siempre un recorrido secuencial (leer 3 páginas es
más barato que usar un índice) y la comparación no demostraría nada. Por eso el script:

1. abre una transacción y carga **200 000 reportes y 400 000 evidencias sintéticos** (varios años de
   operación de una ciudad mediana), y ejecuta `ANALYZE`;
2. por cada índice: lo elimina → `EXPLAIN (ANALYZE, BUFFERS)` → lo crea → `ANALYZE` → `EXPLAIN (ANALYZE, BUFFERS)`;
3. termina con `ROLLBACK`. Como el DDL de PostgreSQL es transaccional, la base queda exactamente como
   estaba (el script lo comprueba: 137 reportes después del rollback).

## Experimento 1 — búsqueda de antecedentes de un incidente

**Consulta:** reportes de CONDICIÓN de la misma categoría, dentro de una caja de ±0,0009° (~100 m)
alrededor del incidente demo y anteriores a la fecha del hecho. Es el filtro previo de la función estrella
("la entidad ya sabía"); la distancia exacta con Haversine se calcula solo sobre los candidatos.

**Índice:** `ix_reportes_antecedentes ON reportes (id_categoria, tipo_reporte, latitud) INCLUDE (longitud, fecha_reporte)`

| | Antes | Después |
|---|---|---|
| Plan sobre reportes | Index Scan con `ix_reportes_categoria` (solo categoría) | Index Scan con `ix_reportes_antecedentes` |
| Condición resuelta por el índice | `id_categoria` | `id_categoria`, `tipo_reporte`, rango de `latitud` |
| Filas descartadas por filtro | 25 052 | 539 |
| Páginas leídas (buffers) | 3 477 | 574 |
| Tiempo de ejecución | 8,39 ms | 0,54 ms |

**Conclusión:** la comparación es honesta: el "antes" ya tenía el índice simple de la FK `id_categoria`,
y aun así tenía que leer y descartar todos los reportes de la categoría (unos 25 000). El índice compuesto
pone primero las columnas de igualdad (categoría, tipo) y después la de rango (latitud), que es el orden
en que un B-tree puede usarlas. Así el recorrido baja a una franja de ~100 m de latitud: **15 veces más
rápido y 6 veces menos páginas**. La longitud va en `INCLUDE` porque una segunda condición de rango no
reduciría más el recorrido del B-tree, pero tenerla en el índice evita ir a la tabla a evaluarla.
Este es el índice que usará la función PL/pgSQL de búsqueda de antecedentes de la Entrega 3.

## Experimento 2 — índice de la clave foránea `evidencias.id_reporte`

**Consulta:** fotos del expediente de un caso (casos → incidentes → evidencias). Es lo que ve el abogado.

**Índice:** `ix_evidencias_reporte ON evidencias (id_reporte)`

| | Antes | Después |
|---|---|---|
| Plan sobre evidencias | Parallel Seq Scan (2 workers) + Hash Join | Index Scan + Nested Loop |
| Filas leídas de evidencias | ≈ 400 214 (133 405 por proceso × 3) | 1 |
| Páginas leídas | 7 688 | 8 |
| Tiempo de ejecución | 39,44 ms | 0,069 ms |

**Conclusión:** PostgreSQL **no crea índices para las claves foráneas** (solo para PK y UNIQUE). Sin el
índice, ver la foto de un caso obliga a recorrer las 400 000 evidencias; con el índice, el plan cambia
de Hash Join a Nested Loop y lee 8 páginas: unas **570 veces más rápido**. El mismo índice acelera el
`ON DELETE CASCADE` desde reportes (borrar un reporte no recorre toda la tabla de evidencias). Por eso la
migración 007 indexa todas las FK de joins frecuentes que no estaban cubiertas por una PK o UNIQUE.

## Costo de los índices

Cada índice ocupa espacio y hace un poco más lentos los INSERT y UPDATE. En REDA las consultas
(mapa, expedientes, búsqueda de antecedentes) son muchas más que las escrituras (un ciudadano crea un
reporte una vez), así que el costo se justifica. No se indexaron columnas que no se usan en filtros ni en joins.
