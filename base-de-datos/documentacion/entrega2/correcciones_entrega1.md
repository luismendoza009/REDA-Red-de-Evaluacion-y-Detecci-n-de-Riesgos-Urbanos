# Cambios respecto a la Entrega 1 — qué cambió y por qué

## 1. Cambio de enfoque del negocio (origen de casi todos los cambios)

En la Entrega 1, REDA se planteó como una herramienta para que las entidades **priorizaran** la atención
de fallas urbanas. Después de analizar el modelo de negocio, el enfoque cambió: el cliente es el
**ciudadano**, y REDA es un **sistema de documentación probatoria de fallas en infraestructura pública**.
Cuando una falla (un hueco, una alcantarilla sin tapa) causa un daño, REDA ayuda a documentarlo con
evidencia y lo vincula con los reportes previos del mismo punto, que demuestran que la entidad "ya sabía".
Si el caso es viable, se remite a un abogado (que paga una cuota fija por caso remitido).

El esquema de la Entrega 1 **se conserva completo**: las 10 tablas siguen existiendo, con sus llaves y
sus políticas de borrado. Lo que hay son columnas y restricciones nuevas y 15 tablas adicionales (25 en total).

## 2. Cambios en tablas existentes

| Tabla | Cambio | Por qué |
|---|---|---|
| roles | CHECK con la lista cerrada; nuevos roles ABOGADO y ANALISTA | El nuevo negocio necesita abogados; ANALISTA prepara el rol de solo lectura de la Entrega 3 |
| usuarios | `fecha_registro` y `fecha_consentimiento` de tipo `DATE`; nuevas `activo` y consentimiento; CHECK de correo en minúsculas y de hash BCrypt. Migración 013: `foto_perfil` (UQ), `foto_perfil_mime`, `fecha_foto_perfil` (`DATE`) con CHECK de que los tres van juntos | Las cuentas se desactivan, no se borran (sus reportes son evidencia). Registro del consentimiento (Ley 1581 de 2012). Evita guardar contraseñas en texto plano. La foto se guarda como ruta, no como archivo dentro de la base |
| zonas | nuevas `ciudad`, `nivel_trafico`, `centros_educativos`, `centros_salud`; UNIQUE (nombre, ciudad); CHECK de tipo. Migración 012: `latitud_referencia`, `longitud_referencia`, `fuente_ubicacion` | Supuesto 2 de la Entrega 1: las variables de contexto del IRU se guardan como datos simulados de la zona. La ciudad permite escalar a otras ciudades. El punto de referencia (tomado de OpenStreetMap) permite sugerir el barrio según el punto marcado en el mapa; el supuesto 6 se mantiene: no se usan polígonos |
| estados | "Priorizado" pasa a llamarse **"Validado"** | Ahora el operador no prioriza: verifica que la falla y la evidencia sean reales. Es el paso que habilita abrir un caso |
| reportes | nueva `tipo_reporte` (CONDICION / INCIDENTE); `fecha` pasa a `fecha_reporte` (de tipo `DATE`); CHECK de rango de coordenadas y de longitud mínima de la descripción; UNIQUE (id_reporte, tipo_reporte) | Distinguir antecedentes de incidentes. La fecha ubica el reporte en el calendario. La UNIQUE habilita la FK compuesta de incidentes |
| evidencias | nuevas `tipo_mime`, `tamano_bytes`, `hash_sha256`, `fecha_captura`; `fecha` pasa a `fecha_carga` (ambas `DATE`); CHECK de tipo y tamaño | El hash SHA-256 demuestra que la foto no fue alterada después de cargarla y evita cargar la misma foto dos veces. Valida el tipo de archivo desde la base |
| evaluaciones | nueva `id_usuario` (operador que evalúa); CHECK de al menos un puntaje; fecha de tipo `DATE` | Trazabilidad: saber quién evaluó |
| historial_estados | `fecha` pasa a `fecha_cambio`; CHECK de anterior ≠ nuevo | Un "cambio" al mismo estado no es un cambio |
| reportes_relacionados | nuevas `tipo_relacion` (SIMILAR / ANTECEDENTE), `origen` (AUTOMATICO / MANUAL), `fecha_vinculo` | El vínculo incidente → antecedente es la función central del nuevo enfoque |

Además, todas las restricciones tienen **nombre explícito** (`fk_`, `ck_`, `uq_`) para que los errores
sean legibles y las pruebas puedan verificar qué regla detuvo cada dato inválido.

La migración 014 añade restricciones `CHECK` con nombre para limitar la longitud máxima de los textos
libres, sin cambiar los tipos `TEXT` ni agregar tablas.

## 3. Tablas nuevas

`entidades_publicas`, `abogados`, `incidentes`, `testigos`, `estados_caso`, `casos`, `remisiones`,
`versiones_indice`, `parametros_indice`, `calculos_indice` (migraciones 001–006) y, después,
`postulaciones` y `tokens_recuperacion` (010) y `calificaciones` (011). Están descritas en el diccionario de
datos y justificadas en la auditoría de normalización.

Por qué las tres últimas:

- **postulaciones**: el ciudadano ya no recibe un abogado asignado; los abogados se postulan y el ciudadano
  elige. La elección genera la remisión que se le cobra al abogado. Un índice único parcial garantiza un
  solo abogado elegido por caso.
- **tokens_recuperacion**: recuperación de contraseña con enlaces de un solo uso. Se guarda el hash del
  token, nunca el token.
- **calificaciones**: el ciudadano califica al abogado (1 a 5) al cerrar el caso. El promedio no se guarda
  (dato derivado, 3FN): lo calcula la vista `v_reputacion_abogados`.

También se agregaron `abogados.anios_experiencia` y `abogados.presentacion` y `casos.fecha_consentimiento`
(momento en que el ciudadano autoriza mostrar el resumen anónimo de su caso a los abogados), en la 010.

## 4. Desviaciones respecto a la propuesta inicial del modelo extendido

- `casos.indice_viabilidad` **no se creó**: el IVC es un valor derivado y se guarda, con su desglose y su
  historial, en `calculos_indice`.
- `parametros_indice` se dividió en `versiones_indice` + `parametros_indice` (corrección de 2FN).
- `incidentes.entidad_presuntamente_responsable` (texto) pasó a ser `id_entidad` (FK a entidades_publicas),
  para no repetir nombres de entidades escritos de formas distintas.
- La tabla `auditoria` se deja para la Entrega 3, junto con los triggers que la alimentan.

## 5. Diagrama

El diagrama EER se actualizó a 23 tablas y 31 relaciones (`diagramas/reda_eer.drawio` y `.png`). La versión de la Entrega 1
se conserva en `diagramas/especificacion_eer_v1.json` y en el tag `entrega-1` del repositorio.

## 6. Ubicación de los archivos de la Entrega 1

Desde la Entrega 2 el repositorio se separa por asignatura: todo lo de Base de Datos I está en
`base-de-datos/`. Por eso las rutas que cita el documento de la Entrega 1 cambiaron:

| En la Entrega 1 | Ahora |
|---|---|
| `documentacion/Entrega1_REDA.docx` (y .pdf) | `base-de-datos/documentacion/Entrega1_REDA.docx` (y .pdf) |
| `diagramas/reda_eer.drawio` y `.png` | `base-de-datos/diagramas/reda_eer.drawio` y `.png` (actualizados a 23 tablas) |
| `migraciones/` (vacía) | `base-de-datos/migraciones/` (001 a 014) |

El documento de la Entrega 1 se conserva tal como se entregó (versión con los integrantes y el enlace al
repositorio); los cambios de diseño posteriores se explican en este archivo y no reescribiendo aquel.
El diagrama en notación de Chen de Rafael Rivera está en `base-de-datos/diagramas/reda_eer_chen.drawio` y `.png`.

## Fechas como DATE

La rúbrica de la Entrega 2 exige que las fechas persistidas sean de tipo `DATE`. Las 26 columnas de
fecha se declaran como `DATE` directamente en la migración que crea cada tabla, y
`incidentes.fecha_hora_hecho` pasa a llamarse `incidentes.fecha_hecho`. Antes de la entrega, esta
conversión estaba en una migración aparte (`018_fechas_date.sql`); se integró en las migraciones
originales para que el DDL muestre el tipo final. Se comprobó que el esquema resultante es el mismo
comparando `pg_dump --schema-only` antes y después.

Usar `DATE` pierde la hora y, por tanto, el orden de eventos del mismo día. Según la decisión D34,
un antecedente debe haberse reportado en un día anterior al hecho; los cambios del historial se ordenan
por `id_historial`. Los tokens de recuperación vencen al comenzar el día siguiente y los bloqueos
temporales de inicio de sesión se expresan en días completos.
