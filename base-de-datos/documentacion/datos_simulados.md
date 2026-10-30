# Datos simulados — qué es real, qué es simulado y cómo pasar a datos reales

Todos los datos de `sql/semilla/` son **simulados** y se generan con `scripts/generar_semilla.py`
(semilla aleatoria fija, siempre produce lo mismo). **Ninguna persona de la semilla existe.**

## Inventario

| Dato | Estado | Detalle |
|---|---|---|
| Nombres, correos y teléfonos de usuarios y testigos | Ficticio | Nombres y apellidos combinados al azar. Correos en el dominio reservado `.test`, que no puede existir en internet |
| Contraseñas | Prueba | Todos los usuarios de prueba: `Reda2026*` (hash BCrypt) |
| Tarjetas profesionales de abogados | Ficticio | Números aleatorios; no corresponden a abogados reales |
| Nombres de barrios de Valledupar | Real | 30 barrios, urbanizaciones o sitios de la ciudad; su comuna coincide con el listado de barrios por comuna (ver «Ubicación de los barrios») |
| Punto de referencia de cada barrio | **Real** (OpenStreetMap) | Un lugar dentro del barrio (parque, colegio, plaza o el polígono del barrio), comprobado contra el límite de su comuna en OSM. Es una referencia, **no** el centroide ni el límite oficial del barrio |
| Coordenadas de los reportes | Simulado | Puntos al azar a menos de 170 m del punto de referencia de su barrio (puntos críticos: a menos de 80 m y luego 35 m entre sí). Los 137 reportes quedan más cerca de su propio barrio que de cualquier otro |
| Tráfico, colegios y centros de salud por zona | Simulado | Marcados `[SIMULADO]` en el diccionario |
| Nombres de entidades públicas | Reales o genéricos | La atribución de cada falla a una entidad es simulada. "Empresa prestadora del servicio de aseo" es genérica a propósito |
| Reportes, fotos, incidentes, casos y remisiones | Simulado | Las rutas de las fotos no apuntan a archivos reales; los hashes se derivan del ID |
| Notificaciones | Simulado | Cinco avisos se generan desde cambios de estado de reportes de sus autores, con casos leídos y pendientes |
| Bloqueos de inicio de sesión | Prueba | Cinco direcciones `.test` sin cuenta asociada demuestran contadores progresivos y bloqueo temporal/permanente |
| Pesos de los índices IRU/IVC | Criterio del equipo | Sin validar con expertos (ver abajo) |
| Tarifas por caso remitido | Simulado | 200 000 / 120 000 COP, sin estudio de mercado |

## Escenarios construidos a propósito (para la demo y para las consultas)

- **9 puntos críticos** (uno en la UPC y otro en el Hospital Rosario Pumarejo): varios reportes de condición de la misma categoría en un radio de ~35 m, durante
  meses, seguidos de un incidente. El incidente 18 (alcantarilla sin tapa en el Centro, 21-oct-2026) tiene
  4 antecedentes, el primero del 19-may-2026: es el caso de la demo y **no tiene caso abierto**, para
  crearlo en vivo.
- **9 incidentes aislados** sin antecedentes cercanos, como contraste (casos más débiles).
- Reportes **sin foto** (~15 %), **rechazados** (~8 %) y **solucionados**, para que las consultas de
  "los que no tienen" y de tiempos de atención den resultados no triviales.

## Ubicación de los barrios

**Por qué se cambió (2026-10-26):** las coordenadas eran aproximaciones inventadas. Al revisar el mapa, el
equipo encontró un punto dentro de la Universidad Popular del Cesar marcado como «La Nevada», que en
realidad queda unos 3,7 km al noroccidente. Se reemplazaron por puntos reales.

**Método:**
1. Búsqueda de cada barrio en OpenStreetMap con Nominatim, Overpass y Photon (consulta del 2026-10-26).
   OSM tiene muy pocos barrios de Valledupar dibujados como polígono; cuando no hay polígono se usa un
   lugar con el nombre del barrio (parque, plaza, colegio, casa de justicia).
2. Cada punto se comprobó contra el polígono de su **comuna** en OSM y contra el listado de barrios por
   comuna (Wikipedia, «Anexo:Barrios de Valledupar»; la Alcaldía publica la misma relación como tabla en
   datos.gov.co, conjunto «Comunas y Barrios del Municipio de Valledupar», sin geometría). La comuna 4 no
   está dibujada en OSM: sus puntos se aceptaron por quedar fuera de las otras cinco.
3. Se descartaron los barrios que no se pudieron ubicar con una fuente (Altagracia, Gaitán, Pontevedra,
   Villa Castro, Mareigua, Sicarare, Panamá, El Prado, Las Delicias, San Fernando, Valle Meza, Los
   Fundadores, Villa Miriam, Simón Bolívar, Los Músicos, San Joaquín, Dangond) y los que quedaban encima
   de otro (Guajira, a 121 m de la Plaza Alfonso López; Obrero, a 172 m de Cañaguate).
4. Se mantuvieron 30 zonas en el mismo orden, para que la semilla (aleatoria con semilla fija) siga
   produciendo los mismos usuarios, reportes, casos y la misma demo.

| # | Zona | Comuna | Punto de referencia (lat, lon) | Lugar de OSM usado |
|---|---|---|---|---|
| 1 | Centro | 1 | 10.47765, -73.24455 | Plaza Alfonso López (OSM) |
| 2 | Loperena | 1 | 10.47351, -73.24883 | Colegio Nacional Loperena (OSM) |
| 3 | Cañaguate | 6 | 10.47990, -73.24930 | Manzanas Cañahuates (OSM, landuse=residential) |
| 4 | Santa Rosa | 2 | 10.46030, -73.23930 | Parque Santa Rosa (OSM) |
| 5 | Santo Domingo | 1 | 10.46959, -73.23802 | Colegio Santo Domingo (OSM) |
| 6 | Doce de Octubre | 2 | 10.45710, -73.24501 | Plaza 12 de Octubre (OSM) |
| 7 | Novalito | 6 | 10.48650, -73.25370 | Manzanas Novalito (OSM, landuse=residential) |
| 8 | Villa Clara | 2 | 10.46262, -73.23689 | Barrio Villa Clara (OSM, place=neighbourhood) |
| 9 | Candelaria Sur | 2 | 10.46193, -73.23228 | Candelaria Sur y Villa del Rosario (OSM, place=city_block) |
| 10 | Los Corales | 5 | 10.48995, -73.27040 | Conjunto Los Corales (OSM, landuse=residential) |
| 11 | Los Mayales | 2 | 10.45880, -73.23053 | Parque Los Mayales (OSM) |
| 12 | Rosario Real | 5 | 10.49537, -73.27702 | Urbanización Rosario Real (OSM, landuse=residential) |
| 13 | Villa Bolivariana | 2 | 10.45133, -73.23832 | Unidad Villa Bolivariana (OSM, landuse=residential) |
| 14 | Primero de Mayo | 3 | 10.45397, -73.25294 | Casa de Justicia Primero de Mayo (OSM) |
| 15 | La Nevada | 5 | 10.47657, -73.28280 | Escuela Mixta y Parque La Nevada (OSM) |
| 16 | San Martín | 3 | 10.45059, -73.24872 | Parque San Martín (OSM) |
| 17 | Garupal | 5 | 10.47048, -73.26375 | Barrio Garupal (OSM, landuse=residential) |
| 18 | Doña Clara | 2 | 10.45456, -73.22862 | Urbanización Doña Clara (OSM, landuse=residential) |
| 19 | Universidad Popular del Cesar | 4 | 10.44950, -73.26240 | UPC sede Sabanas (OSM, amenity=university) |
| 20 | Iracal | 5 | 10.47438, -73.26801 | Parque Iracal (OSM) |
| 21 | Los Cortijos | 5 | 10.47632, -73.26188 | Parque Los Cortijos (OSM) |
| 22 | Don Carmelo | 3 | 10.44097, -73.26020 | Barrio Don Carmelo (OSM, place=neighbourhood) |
| 23 | Nuevo Milenio | 3 | 10.44221, -73.25171 | Barrio Milenio (OSM, place=neighbourhood) |
| 24 | Verona | 4 | 10.44528, -73.26427 | Urbanización Verona (OSM, landuse=residential) |
| 25 | Terminal de Transportes | 3 | 10.44436, -73.24302 | Terminal de Transporte de Valledupar (OSM, amenity=bus_station) |
| 26 | Leandro Díaz | 3 | 10.43640, -73.26400 | Urbanización Leandro Díaz (OSM, landuse=residential) |
| 27 | Vive Alto | 2 | 10.44038, -73.23392 | Conjunto Vive Alto (OSM, landuse=residential) |
| 28 | Don Alberto | 5 | 10.46630, -73.28850 | Urbanización Norte y Altos de Don Alberto (OSM) |
| 29 | Las Trinitarias | 5 | 10.49801, -73.27514 | Las Trinitarias (OSM, landuse=residential) |
| 30 | Hospital Rosario Pumarejo | 1 | 10.46933, -73.25452 | Hospital Rosario Pumarejo de López (OSM, amenity=hospital) |

La migración 012 guarda este punto y su fuente en `zonas` (`latitud_referencia`, `longitud_referencia`,
`fuente_ubicacion`). Con él la aplicación **sugiere el barrio** cuando el ciudadano marca el punto en el
mapa (si algún barrio queda a 600 m o menos); el ciudadano puede cambiarlo.

**Límite conocido:** solo 30 de los ~175 barrios de la ciudad están ubicados. Un punto en un barrio no
ubicado no recibe sugerencia, o recibe la del barrio vecino si queda a menos de 600 m. Para producción
hacen falta los polígonos oficiales de Planeación Municipal.

## Cómo pasar a datos reales (proyecto de grado)

| Necesidad | Fuente posible | Estado |
|---|---|---|
| División barrial y polígonos | Planeación Municipal de Valledupar / POT | En datos.gov.co solo está la **tabla** de barrios por comuna, sin polígonos (revisado el 2026-10-26); hay que pedir la cartografía del POT |
| Cartografía base | OpenStreetMap (licencia ODbL) | Disponible; es la que usará el mapa (Leaflet) |
| Colegios y centros de salud | Directorios oficiales de establecimientos educativos y de prestadores de salud | **Por verificar** cobertura y formato |
| Tráfico por zona | Conteos de la Secretaría de Tránsito, o encuestas | Probablemente seguirá simulado |
| Pesos de IRU e IVC | Encuestas a funcionarios y abogados, con un método formal (p. ej. AHP) | Pendiente |
| Reportes reales | La propia plataforma, en un piloto | Pendiente |

**Cómo evitar que lo simulado invalide conclusiones:** cualquier análisis con datos reales debe
separarse de los simulados (otra base, o una columna de origen en cada tabla). Las variables que sigan
simuladas no pueden usarse para afirmar nada sobre la ciudad real; solo sirven para demostrar que el
cálculo funciona.
