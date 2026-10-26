# Datos simulados — qué es real, qué es simulado y cómo pasar a datos reales

Todos los datos de `sql/semilla/` son **simulados** y se generan con `scripts/generar_semilla.py`
(semilla aleatoria fija, siempre produce lo mismo). **Ninguna persona de la semilla existe.**

## Inventario

| Dato | Estado | Detalle |
|---|---|---|
| Nombres, correos y teléfonos de usuarios y testigos | Ficticio | Nombres y apellidos combinados al azar. Correos en el dominio reservado `.test`, que no puede existir en internet |
| Contraseñas | Prueba | Todos los usuarios de prueba: `Reda2026*` (hash BCrypt) |
| Tarjetas profesionales de abogados | Ficticio | Números aleatorios; no corresponden a abogados reales |
| Nombres de barrios de Valledupar | Real, **por verificar** | Tomados del conocimiento general de la ciudad; falta contrastarlos con la división político-administrativa oficial (Planeación Municipal) |
| Coordenadas de los barrios y de los reportes | Simulado | Centroides aproximados dentro del casco urbano y puntos aleatorios a su alrededor. No deben usarse para ubicar nada real |
| Tráfico, colegios y centros de salud por zona | Simulado | Marcados `[SIMULADO]` en el diccionario |
| Nombres de entidades públicas | Reales o genéricos | La atribución de cada falla a una entidad es simulada. "Empresa prestadora del servicio de aseo" es genérica a propósito |
| Reportes, fotos, incidentes, casos y remisiones | Simulado | Las rutas de las fotos no apuntan a archivos reales; los hashes se derivan del ID |
| Pesos de los índices IRU/IVC | Criterio del equipo | Sin validar con expertos (ver abajo) |
| Tarifas por caso remitido | Simulado | 200 000 / 120 000 COP, sin estudio de mercado |

## Escenarios construidos a propósito (para la demo y para las consultas)

- **9 puntos críticos**: varios reportes de condición de la misma categoría en un radio de ~35 m, durante
  meses, seguidos de un incidente. El incidente 18 (alcantarilla sin tapa en el Centro, 21-oct-2026) tiene
  4 antecedentes, el primero del 19-may-2026: es el caso de la demo y **no tiene caso abierto**, para
  crearlo en vivo.
- **9 incidentes aislados** sin antecedentes cercanos, como contraste (casos más débiles).
- Reportes **sin foto** (~15 %), **rechazados** (~8 %) y **solucionados**, para que las consultas de
  "los que no tienen" y de tiempos de atención den resultados no triviales.

## Cómo pasar a datos reales (proyecto de grado)

| Necesidad | Fuente posible | Estado |
|---|---|---|
| División barrial y polígonos | Planeación Municipal de Valledupar / POT; portal de datos abiertos del Estado colombiano | **Por verificar** que existan y sean descargables |
| Cartografía base | OpenStreetMap (licencia ODbL) | Disponible; es la que usará el mapa (Leaflet) |
| Colegios y centros de salud | Directorios oficiales de establecimientos educativos y de prestadores de salud | **Por verificar** cobertura y formato |
| Tráfico por zona | Conteos de la Secretaría de Tránsito, o encuestas | Probablemente seguirá simulado |
| Pesos de IRU e IVC | Encuestas a funcionarios y abogados, con un método formal (p. ej. AHP) | Pendiente |
| Reportes reales | La propia plataforma, en un piloto | Pendiente |

**Cómo evitar que lo simulado invalide conclusiones:** cualquier análisis con datos reales debe
separarse de los simulados (otra base, o una columna de origen en cada tabla). Las variables que sigan
simuladas no pueden usarse para afirmar nada sobre la ciudad real; solo sirven para demostrar que el
cálculo funciona.
