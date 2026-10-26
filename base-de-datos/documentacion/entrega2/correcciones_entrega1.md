# Cambios respecto a la Entrega 1 — qué cambió y por qué

## 1. Cambio de enfoque del negocio (origen de casi todos los cambios)

En la Entrega 1, REDA se planteó como una herramienta para que las entidades **priorizaran** la atención
de fallas urbanas. Después de analizar el modelo de negocio, el enfoque cambió: el cliente es el
**ciudadano**, y REDA es un **sistema de documentación probatoria de fallas en infraestructura pública**.
Cuando una falla (un hueco, una alcantarilla sin tapa) causa un daño, REDA ayuda a documentarlo con
evidencia y lo vincula con los reportes previos del mismo punto, que demuestran que la entidad "ya sabía".
Si el caso es viable, se remite a un abogado (que paga una cuota fija por caso remitido).

El esquema de la Entrega 1 **se conserva completo**: las 10 tablas siguen existiendo, con sus llaves y
sus políticas de borrado. Lo que hay son columnas y restricciones nuevas y 10 tablas adicionales.

## 2. Cambios en tablas existentes

| Tabla | Cambio | Por qué |
|---|---|---|
| roles | CHECK con la lista cerrada; nuevos roles ABOGADO y ANALISTA | El nuevo negocio necesita abogados; ANALISTA prepara el rol de solo lectura de la Entrega 3 |
| usuarios | `fecha_registro` pasa a TIMESTAMPTZ; nuevas `activo` y `fecha_consentimiento`; CHECK de correo en minúsculas y de hash BCrypt | Las cuentas se desactivan, no se borran (sus reportes son evidencia). Registro del consentimiento (Ley 1581 de 2012). Evita guardar contraseñas en texto plano |
| zonas | nuevas `ciudad`, `nivel_trafico`, `centros_educativos`, `centros_salud`; UNIQUE (nombre, ciudad); CHECK de tipo | Supuesto 2 de la Entrega 1: las variables de contexto del IRU se guardan como datos simulados de la zona. La ciudad permite escalar a otras ciudades |
| estados | "Priorizado" pasa a llamarse **"Validado"** | Ahora el operador no prioriza: verifica que la falla y la evidencia sean reales. Es el paso que habilita abrir un caso |
| reportes | nueva `tipo_reporte` (CONDICION / INCIDENTE); `fecha` pasa a `fecha_reporte` TIMESTAMPTZ; CHECK de rango de coordenadas y de longitud mínima de la descripción; UNIQUE (id_reporte, tipo_reporte) | Distinguir antecedentes de incidentes. El sello de tiempo con zona horaria tiene valor probatorio. La UNIQUE habilita la FK compuesta de incidentes |
| evidencias | nuevas `tipo_mime`, `tamano_bytes`, `hash_sha256`, `fecha_captura`; `fecha` pasa a `fecha_carga` TIMESTAMPTZ; CHECK de tipo y tamaño | El hash SHA-256 demuestra que la foto no fue alterada después de cargarla y evita cargar la misma foto dos veces. Valida el tipo de archivo desde la base |
| evaluaciones | nueva `id_usuario` (operador que evalúa); CHECK de al menos un puntaje; fecha TIMESTAMPTZ | Trazabilidad: saber quién evaluó |
| historial_estados | `fecha` pasa a `fecha_cambio`; CHECK de anterior ≠ nuevo | Un "cambio" al mismo estado no es un cambio |
| reportes_relacionados | nuevas `tipo_relacion` (SIMILAR / ANTECEDENTE), `origen` (AUTOMATICO / MANUAL), `fecha_vinculo` | El vínculo incidente → antecedente es la función central del nuevo enfoque |

Además, todas las restricciones tienen **nombre explícito** (`fk_`, `ck_`, `uq_`) para que los errores
sean legibles y las pruebas puedan verificar qué regla detuvo cada dato inválido.

## 3. Tablas nuevas

`entidades_publicas`, `abogados`, `incidentes`, `testigos`, `estados_caso`, `casos`, `remisiones`,
`versiones_indice`, `parametros_indice`, `calculos_indice`. Están descritas en el diccionario de datos y
justificadas en la auditoría de normalización.

## 4. Desviaciones respecto a la propuesta inicial del modelo extendido

- `casos.indice_viabilidad` **no se creó**: el IVC es un valor derivado y se guarda, con su desglose y su
  historial, en `calculos_indice`.
- `parametros_indice` se dividió en `versiones_indice` + `parametros_indice` (corrección de 2FN).
- `incidentes.entidad_presuntamente_responsable` (texto) pasó a ser `id_entidad` (FK a entidades_publicas),
  para no repetir nombres de entidades escritos de formas distintas.
- La tabla `auditoria` se deja para la Entrega 3, junto con los triggers que la alimentan.

## 5. Diagrama

El diagrama EER se actualizó a 20 tablas (`diagramas/reda_eer.drawio` y `.png`). La versión de la Entrega 1
se conserva en `diagramas/especificacion_eer_v1.json` y en el tag `entrega-1` del repositorio.
