# Diccionario de datos - REDA

> Generado automaticamente desde el esquema real (`scripts/generar_diccionario.ps1`). No editar a mano:
> los significados vienen de los `COMMENT ON` de las migraciones.
>
> Migraciones aplicadas: 001_catalogos.sql, 002_usuarios.sql, 003_reportes.sql, 004_incidentes.sql, 005_casos.sql, 006_indices_priorizacion.sql, 007_indices.sql, 008_vistas.sql, 009_situacion_casos_mapa.sql, 010_postulaciones_y_cuentas.sql, 011_calificaciones.sql, 012_ubicacion_zonas.sql, 013_foto_perfil.sql
>
> Etiquetas: **[PERSONAL]** dato personal (Ley 1581 de 2012) - **[SENSIBLE]** dato sensible de salud -
> **[SIMULADO]** valor simulado para el proyecto de aula. La medida de proteccion se indica en cada columna.
> Claves: PK primaria, FK foranea, UQ unica.

## roles

Catálogo de roles de la aplicación (Ciudadano, Operador, Abogado, Administrador, Analista).

| Columna | Tipo | Nulo | Claves | Defecto | Significado de negocio |
|---|---|---|---|---|---|
| id_rol | INTEGER | No | PK | identidad | Identificador único del rol. |
| nombre | VARCHAR(30) | No | UQ |  | Nombre del rol en mayúsculas; lista cerrada controlada por CHECK. |

**Restricciones:**

- `ck_roles_nombre`: `CHECK (((nombre)::text = ANY ((ARRAY['CIUDADANO'::character varying, 'OPERADOR'::character varying, 'ABOGADO'::character varying, 'ADMINISTRADOR'::character varying, 'ANALISTA'::character varying])::text[])))`
- `uq_roles_nombre`: `UNIQUE (nombre)`

## categorias

Tipos de falla de infraestructura que puede clasificar un reporte (hueco, alcantarilla, luminaria...).

| Columna | Tipo | Nulo | Claves | Defecto | Significado de negocio |
|---|---|---|---|---|---|
| id_categoria | INTEGER | No | PK | identidad | Identificador único de la categoría. |
| nombre | VARCHAR(80) | No | UQ |  | Nombre de la categoría (único). |
| descripcion | TEXT | Sí |  |  | Qué tipo de falla cubre la categoría. |

**Restricciones:**

- `uq_categorias_nombre`: `UNIQUE (nombre)`

## zonas

Barrios o zonas de la ciudad; permiten agregar reportes y aportan variables de contexto al IRU.

| Columna | Tipo | Nulo | Claves | Defecto | Significado de negocio |
|---|---|---|---|---|---|
| id_zona | INTEGER | No | PK | identidad | Identificador único de la zona. |
| nombre | VARCHAR(100) | No | UQ |  | Nombre del barrio o zona (único dentro de la ciudad). |
| tipo | VARCHAR(20) | No |  |  | Uso predominante: RESIDENCIAL, COMERCIAL, ESCOLAR, MIXTA o INSTITUCIONAL. |
| ciudad | VARCHAR(80) | No | UQ | 'Valledupar' | Ciudad a la que pertenece la zona (el sistema está pensado para escalar a otras ciudades). |
| nivel_trafico | SMALLINT | No |  | 3 | [SIMULADO] Nivel de tráfico vehicular/peatonal de 1 (bajo) a 5 (muy alto). |
| centros_educativos | SMALLINT | No |  | 0 | [SIMULADO] Número de colegios o centros educativos en la zona. |
| centros_salud | SMALLINT | No |  | 0 | [SIMULADO] Número de hospitales, clínicas o centros de salud en la zona. |
| latitud_referencia | NUMERIC(9,6) | Sí |  |  | Latitud de un punto de referencia dentro del barrio (OpenStreetMap). No es el centroide exacto. |
| longitud_referencia | NUMERIC(9,6) | Sí |  |  | Longitud del punto de referencia del barrio (OpenStreetMap). |
| fuente_ubicacion | VARCHAR(200) | Sí |  |  | De dónde sale el punto de referencia (lugar de OpenStreetMap y fecha de consulta). |

**Restricciones:**

- `ck_zonas_educativos`: `CHECK ((centros_educativos >= 0))`
- `ck_zonas_fuente`: `CHECK (((fuente_ubicacion IS NULL) OR (latitud_referencia IS NOT NULL)))`
- `ck_zonas_latitud`: `CHECK (((latitud_referencia >= ('-90'::integer)::numeric) AND (latitud_referencia <= (90)::numeric)))`
- `ck_zonas_longitud`: `CHECK (((longitud_referencia >= ('-180'::integer)::numeric) AND (longitud_referencia <= (180)::numeric)))`
- `ck_zonas_salud`: `CHECK ((centros_salud >= 0))`
- `ck_zonas_tipo`: `CHECK (((tipo)::text = ANY ((ARRAY['RESIDENCIAL'::character varying, 'COMERCIAL'::character varying, 'ESCOLAR'::character varying, 'MIXTA'::character varying, 'INSTITUCIONAL'::character varying])::text[])))`
- `ck_zonas_trafico`: `CHECK (((nivel_trafico >= 1) AND (nivel_trafico <= 5)))`
- `ck_zonas_ubicacion_completa`: `CHECK (((latitud_referencia IS NULL) = (longitud_referencia IS NULL)))`
- `uq_zonas_nombre_ciudad`: `UNIQUE (nombre, ciudad)`

## estados

Catálogo de estados de un reporte: Pendiente, En revisión, Validado, En proceso, Solucionado, Rechazado.

| Columna | Tipo | Nulo | Claves | Defecto | Significado de negocio |
|---|---|---|---|---|---|
| id_estado | INTEGER | No | PK | identidad | Identificador único del estado. |
| nombre | VARCHAR(30) | No | UQ |  | Nombre del estado (único). |

**Restricciones:**

- `uq_estados_nombre`: `UNIQUE (nombre)`

## estados_caso

Catálogo de estados de un caso legal: Borrador, Evaluado, Remitido, Aceptado por abogado, Rechazado, Cerrado.

| Columna | Tipo | Nulo | Claves | Defecto | Significado de negocio |
|---|---|---|---|---|---|
| id_estado_caso | INTEGER | No | PK | identidad | Identificador único del estado de caso. |
| nombre | VARCHAR(40) | No | UQ |  | Nombre del estado de caso (único). |

**Restricciones:**

- `uq_estados_caso_nombre`: `UNIQUE (nombre)`

## entidades_publicas

Entidades a las que se atribuye la responsabilidad de mantener la infraestructura (posible parte demandada).

| Columna | Tipo | Nulo | Claves | Defecto | Significado de negocio |
|---|---|---|---|---|---|
| id_entidad | INTEGER | No | PK | identidad | Identificador único de la entidad. |
| nombre | VARCHAR(150) | No | UQ |  | Nombre oficial de la entidad (único). |
| tipo | VARCHAR(30) | No |  |  | ALCALDIA, SECRETARIA, EMPRESA_SERVICIOS, GOBERNACION, NACIONAL u OTRA. |
| ciudad | VARCHAR(80) | No |  | 'Valledupar' | Ciudad de jurisdicción de la entidad. |

**Restricciones:**

- `ck_entidades_tipo`: `CHECK (((tipo)::text = ANY ((ARRAY['ALCALDIA'::character varying, 'SECRETARIA'::character varying, 'EMPRESA_SERVICIOS'::character varying, 'GOBERNACION'::character varying, 'NACIONAL'::character varying, 'OTRA'::character varying])::text[])))`
- `uq_entidades_nombre`: `UNIQUE (nombre)`

## usuarios

Personas registradas en la plataforma (ciudadanos, operadores, abogados, administradores).

| Columna | Tipo | Nulo | Claves | Defecto | Significado de negocio |
|---|---|---|---|---|---|
| id_usuario | INTEGER | No | PK | identidad | Identificador único del usuario. |
| nombre | VARCHAR(120) | No |  |  | [PERSONAL] Nombre completo. Protección: no se expone en vistas públicas ni al rol de análisis. |
| correo | VARCHAR(150) | No | UQ |  | [PERSONAL] Correo electrónico en minúsculas, único; usado para autenticación. Protección: oculto en vistas públicas. |
| password_hash | VARCHAR(255) | No |  |  | Hash BCrypt de la contraseña (nunca texto plano). Protección: ningún rol de lectura tiene acceso. |
| id_rol | INTEGER | No | FK |  | Rol de la aplicación asignado al usuario. |
| fecha_registro | TIMESTAMPTZ | No |  | now() | Momento de creación de la cuenta. |
| activo | BOOLEAN | No |  | true | false = cuenta desactivada (no se borra: sus reportes son evidencia histórica). |
| fecha_consentimiento | TIMESTAMPTZ | No |  | now() | Momento en que el usuario aceptó la política de tratamiento de datos (Ley 1581 de 2012). |
| foto_perfil | VARCHAR(255) | Sí | UQ |  | [PERSONAL] Ruta relativa (nombre generado) de la foto de perfil. Solo la ven el propio usuario y el personal; la de un abogado verificado, cualquier usuario con sesión. |
| foto_perfil_mime | VARCHAR(20) | Sí |  |  | Formato real de la foto de perfil (detectado por sus bytes): image/jpeg, image/png o image/webp. |
| fecha_foto_perfil | TIMESTAMPTZ | Sí |  |  | Cuándo se cargó la foto de perfil actual. |

**Restricciones:**

- `ck_usuarios_correo`: `CHECK ((((correo)::text = lower((correo)::text)) AND ((correo)::text ~~ '%_@_%._%'::text)))`
- `ck_usuarios_foto_completa`: `CHECK ((((foto_perfil IS NULL) AND (foto_perfil_mime IS NULL) AND (fecha_foto_perfil IS NULL)) OR ((foto_perfil IS NOT NULL) AND (foto_perfil_mime IS NOT NULL) AND (fecha_foto_perfil IS NOT NULL))))`
- `ck_usuarios_foto_mime`: `CHECK (((foto_perfil_mime)::text = ANY ((ARRAY['image/jpeg'::character varying, 'image/png'::character varying, 'image/webp'::character varying])::text[])))`
- `ck_usuarios_hash`: `CHECK ((((password_hash)::text ~~ '$2_$%'::text) AND (length((password_hash)::text) = 60)))`
- `fk_usuarios_rol`: `FOREIGN KEY (id_rol) REFERENCES roles(id_rol) ON DELETE RESTRICT`
- `uq_usuarios_correo`: `UNIQUE (correo)`
- `uq_usuarios_foto`: `UNIQUE (foto_perfil)`

**Índices adicionales:**

- `ix_usuarios_rol`: `btree (id_rol)`

## abogados

Abogados o firmas aliadas que reciben casos remitidos (especialización 1:1 de usuarios).

| Columna | Tipo | Nulo | Claves | Defecto | Significado de negocio |
|---|---|---|---|---|---|
| id_abogado | INTEGER | No | PK | identidad | Identificador único del abogado. |
| id_usuario | INTEGER | No | FK, UQ |  | Cuenta de usuario del abogado (relación 1:1, UNIQUE). |
| tarjeta_profesional | VARCHAR(20) | No | UQ |  | [PERSONAL] Número de tarjeta profesional (solo dígitos, único). |
| especialidad | VARCHAR(80) | No |  |  | Área de práctica (p. ej. responsabilidad del Estado, derecho administrativo). |
| ciudad | VARCHAR(80) | No |  | 'Valledupar' | Ciudad donde ejerce; se usa para asignar casos. |
| activo | BOOLEAN | No |  | true | false = pendiente de verificación de la tarjeta profesional o suspendido: no ve ni toma casos. |
| anios_experiencia | SMALLINT | No |  | 0 | Años de ejercicio declarados por el abogado (se muestran al ciudadano al comparar). |
| presentacion | TEXT | Sí |  |  | Texto con que el abogado se presenta a los ciudadanos. |

**Restricciones:**

- `ck_abogados_experiencia`: `CHECK (((anios_experiencia >= 0) AND (anios_experiencia <= 70)))`
- `ck_abogados_tarjeta`: `CHECK (((tarjeta_profesional)::text ~ '^[0-9]{4,10}$'::text))`
- `fk_abogados_usuario`: `FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE RESTRICT`
- `uq_abogados_tarjeta`: `UNIQUE (tarjeta_profesional)`
- `uq_abogados_usuario`: `UNIQUE (id_usuario)`

## reportes

Entidad central: cada falla de infraestructura reportada. CONDICION = antecedente sin daño; INCIDENTE = hubo daño a una persona o bien.

| Columna | Tipo | Nulo | Claves | Defecto | Significado de negocio |
|---|---|---|---|---|---|
| id_reporte | INTEGER | No | PK, UQ | identidad | Identificador único del reporte. |
| id_usuario | INTEGER | No | FK |  | Ciudadano que creó el reporte. |
| id_categoria | INTEGER | No | FK |  | Categoría de la falla. |
| id_zona | INTEGER | No | FK |  | Barrio o zona donde está la falla. |
| id_estado | INTEGER | No | FK |  | Estado actual del reporte (el historial queda en historial_estados). |
| tipo_reporte | VARCHAR(10) | No | UQ | 'CONDICION' | CONDICION (antecedente probatorio) o INCIDENTE (hubo daño; tiene fila en incidentes). |
| descripcion | TEXT | No |  |  | Descripción libre de la falla (mínimo 10 caracteres). |
| latitud | NUMERIC(9,6) | No |  |  | Latitud GPS (WGS84) de la falla. En el mapa público se muestra sin datos del autor. |
| longitud | NUMERIC(9,6) | No |  |  | Longitud GPS (WGS84) de la falla. |
| fecha_reporte | TIMESTAMPTZ | No |  | now() | Momento en que se registró el reporte (sello de tiempo probatorio). |
| nivel_gravedad | SMALLINT | Sí |  |  | Gravedad percibida por el ciudadano, 1 a 5 (opcional). No es el IRU. |

**Restricciones:**

- `ck_reportes_descripcion`: `CHECK ((length(TRIM(BOTH FROM descripcion)) >= 10))`
- `ck_reportes_gravedad`: `CHECK (((nivel_gravedad >= 1) AND (nivel_gravedad <= 5)))`
- `ck_reportes_latitud`: `CHECK (((latitud >= ('-90'::integer)::numeric) AND (latitud <= (90)::numeric)))`
- `ck_reportes_longitud`: `CHECK (((longitud >= ('-180'::integer)::numeric) AND (longitud <= (180)::numeric)))`
- `ck_reportes_tipo`: `CHECK (((tipo_reporte)::text = ANY ((ARRAY['CONDICION'::character varying, 'INCIDENTE'::character varying])::text[])))`
- `fk_reportes_categoria`: `FOREIGN KEY (id_categoria) REFERENCES categorias(id_categoria) ON DELETE RESTRICT`
- `fk_reportes_estado`: `FOREIGN KEY (id_estado) REFERENCES estados(id_estado) ON DELETE RESTRICT`
- `fk_reportes_usuario`: `FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE RESTRICT`
- `fk_reportes_zona`: `FOREIGN KEY (id_zona) REFERENCES zonas(id_zona) ON DELETE RESTRICT`
- `uq_reportes_id_tipo`: `UNIQUE (id_reporte, tipo_reporte)`

**Índices adicionales:**

- `ix_reportes_antecedentes`: `btree (id_categoria, tipo_reporte, latitud) INCLUDE (longitud, fecha_reporte)`
- `ix_reportes_categoria`: `btree (id_categoria)`
- `ix_reportes_estado`: `btree (id_estado)`
- `ix_reportes_usuario`: `btree (id_usuario)`
- `ix_reportes_zona`: `btree (id_zona)`

## evidencias

Fotografías de soporte de un reporte (1 reporte : N evidencias).

| Columna | Tipo | Nulo | Claves | Defecto | Significado de negocio |
|---|---|---|---|---|---|
| id_evidencia | INTEGER | No | PK | identidad | Identificador único de la evidencia. |
| id_reporte | INTEGER | No | FK |  | Reporte al que pertenece la evidencia. |
| ruta_imagen | VARCHAR(255) | No | UQ |  | Ruta relativa del archivo en el servidor, con nombre generado (no el original). |
| tipo_mime | VARCHAR(30) | No |  |  | Tipo de archivo permitido: image/jpeg, image/png o image/webp. |
| tamano_bytes | INTEGER | No |  |  | Tamaño del archivo en bytes (máximo 10 MB). |
| hash_sha256 | CHAR(64) | No | UQ |  | Huella SHA-256 del archivo; prueba de integridad y evita cargar la misma foto dos veces. |
| fecha_captura | TIMESTAMPTZ | Sí |  |  | Fecha de toma de la foto según sus metadatos (si existen). [PERSONAL] Los metadatos EXIF pueden revelar ubicación. |
| fecha_carga | TIMESTAMPTZ | No |  | now() | Momento en que la foto se subió a la plataforma. |

**Restricciones:**

- `ck_evidencias_fechas`: `CHECK (((fecha_captura IS NULL) OR (fecha_captura <= fecha_carga)))`
- `ck_evidencias_hash`: `CHECK ((hash_sha256 ~ '^[0-9a-f]{64}$'::text))`
- `ck_evidencias_mime`: `CHECK (((tipo_mime)::text = ANY ((ARRAY['image/jpeg'::character varying, 'image/png'::character varying, 'image/webp'::character varying])::text[])))`
- `ck_evidencias_tamano`: `CHECK (((tamano_bytes > 0) AND (tamano_bytes <= 10485760)))`
- `fk_evidencias_reporte`: `FOREIGN KEY (id_reporte) REFERENCES reportes(id_reporte) ON DELETE CASCADE`
- `uq_evidencias_hash`: `UNIQUE (hash_sha256)`
- `uq_evidencias_ruta`: `UNIQUE (ruta_imagen)`

**Índices adicionales:**

- `ix_evidencias_reporte`: `btree (id_reporte)`

## evaluaciones

Evaluación técnica de un reporte hecha por un operador; insumo del IRU.

| Columna | Tipo | Nulo | Claves | Defecto | Significado de negocio |
|---|---|---|---|---|---|
| id_evaluacion | INTEGER | No | PK | identidad | Identificador único de la evaluación. |
| id_reporte | INTEGER | No | FK |  | Reporte evaluado. |
| id_usuario | INTEGER | No | FK |  | Operador que hizo la evaluación. |
| gravedad | SMALLINT | Sí |  |  | Gravedad técnica 0-100. |
| impacto | SMALLINT | Sí |  |  | Impacto potencial 0-100 (personas/vehículos expuestos). |
| frecuencia | SMALLINT | Sí |  |  | Recurrencia del problema 0-100. |
| fecha_evaluacion | TIMESTAMPTZ | No |  | now() | Momento de la evaluación. |

**Restricciones:**

- `ck_evaluaciones_alguna`: `CHECK ((num_nonnulls(gravedad, impacto, frecuencia) > 0))`
- `ck_evaluaciones_frecuencia`: `CHECK (((frecuencia >= 0) AND (frecuencia <= 100)))`
- `ck_evaluaciones_gravedad`: `CHECK (((gravedad >= 0) AND (gravedad <= 100)))`
- `ck_evaluaciones_impacto`: `CHECK (((impacto >= 0) AND (impacto <= 100)))`
- `fk_evaluaciones_reporte`: `FOREIGN KEY (id_reporte) REFERENCES reportes(id_reporte) ON DELETE CASCADE`
- `fk_evaluaciones_usuario`: `FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE RESTRICT`

**Índices adicionales:**

- `ix_evaluaciones_reporte`: `btree (id_reporte)`
- `ix_evaluaciones_usuario`: `btree (id_usuario)`

## historial_estados

Bitácora de cada cambio de estado de un reporte: quién, cuándo, de qué estado a cuál y por qué.

| Columna | Tipo | Nulo | Claves | Defecto | Significado de negocio |
|---|---|---|---|---|---|
| id_historial | INTEGER | No | PK | identidad | Identificador único del registro. |
| id_reporte | INTEGER | No | FK |  | Reporte cuyo estado cambió. |
| id_estado_anterior | INTEGER | No | FK |  | Estado antes del cambio. |
| id_estado_nuevo | INTEGER | No | FK |  | Estado después del cambio (distinto del anterior). |
| id_usuario | INTEGER | No | FK |  | Operador o administrador que hizo el cambio. |
| fecha_cambio | TIMESTAMPTZ | No |  | now() | Momento del cambio. |
| observacion | TEXT | Sí |  |  | Justificación u observación del cambio. |

**Restricciones:**

- `ck_historial_distintos`: `CHECK ((id_estado_anterior <> id_estado_nuevo))`
- `fk_historial_anterior`: `FOREIGN KEY (id_estado_anterior) REFERENCES estados(id_estado) ON DELETE RESTRICT`
- `fk_historial_nuevo`: `FOREIGN KEY (id_estado_nuevo) REFERENCES estados(id_estado) ON DELETE RESTRICT`
- `fk_historial_reporte`: `FOREIGN KEY (id_reporte) REFERENCES reportes(id_reporte) ON DELETE CASCADE`
- `fk_historial_usuario`: `FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE RESTRICT`

**Índices adicionales:**

- `ix_historial_anterior`: `btree (id_estado_anterior)`
- `ix_historial_nuevo`: `btree (id_estado_nuevo)`
- `ix_historial_reporte`: `btree (id_reporte, fecha_cambio)`
- `ix_historial_usuario`: `btree (id_usuario)`

## reportes_relacionados

Relación M:N reflexiva entre reportes: similares o antecedentes de un incidente.

| Columna | Tipo | Nulo | Claves | Defecto | Significado de negocio |
|---|---|---|---|---|---|
| id_reporte | INTEGER | No | FK, PK |  | Reporte origen (en ANTECEDENTE, el incidente). |
| id_reporte_relacionado | INTEGER | No | FK, PK |  | Reporte vinculado (en ANTECEDENTE, la condición previa). |
| tipo_relacion | VARCHAR(12) | No |  | 'SIMILAR' | SIMILAR o ANTECEDENTE. |
| origen | VARCHAR(10) | No |  | 'AUTOMATICO' | AUTOMATICO (lo calculó el sistema por cercanía) o MANUAL (lo vinculó un operador). |
| fecha_vinculo | TIMESTAMPTZ | No |  | now() | Momento en que se creó el vínculo. |

**Restricciones:**

- `ck_relacionados_distintos`: `CHECK ((id_reporte <> id_reporte_relacionado))`
- `ck_relacionados_origen`: `CHECK (((origen)::text = ANY ((ARRAY['AUTOMATICO'::character varying, 'MANUAL'::character varying])::text[])))`
- `ck_relacionados_tipo`: `CHECK (((tipo_relacion)::text = ANY ((ARRAY['SIMILAR'::character varying, 'ANTECEDENTE'::character varying])::text[])))`
- `fk_relacionados_relacionado`: `FOREIGN KEY (id_reporte_relacionado) REFERENCES reportes(id_reporte) ON DELETE CASCADE`
- `fk_relacionados_reporte`: `FOREIGN KEY (id_reporte) REFERENCES reportes(id_reporte) ON DELETE CASCADE`

**Índices adicionales:**

- `ix_relacionados_relacionado`: `btree (id_reporte_relacionado)`

## incidentes

Daño sufrido por una persona o bien a causa de una falla (especialización 1:1 de reportes tipo INCIDENTE).

| Columna | Tipo | Nulo | Claves | Defecto | Significado de negocio |
|---|---|---|---|---|---|
| id_incidente | INTEGER | No | PK | identidad | Identificador único del incidente. |
| id_reporte | INTEGER | No | FK, UQ |  | Reporte de tipo INCIDENTE al que pertenece (1:1). |
| tipo_reporte | VARCHAR(10) | No | FK | 'INCIDENTE' | Siempre INCIDENTE; existe solo para la FK compuesta que garantiza el tipo del reporte. |
| id_entidad | INTEGER | Sí | FK |  | Entidad a la que se atribuye presuntamente la falla (nulo si aún no se identifica). |
| fecha_hora_hecho | TIMESTAMPTZ | No |  |  | Fecha y hora exactas en que ocurrió el daño (puede ser anterior al reporte). Inicia el plazo de caducidad. |
| tipo_dano | VARCHAR(15) | No |  |  | LESION, DANO_MATERIAL o AMBOS. [SENSIBLE] si incluye lesión. |
| descripcion_dano | TEXT | No |  |  | [SENSIBLE] Descripción del daño; puede incluir información de salud. Protección: no visible al rol de análisis ni en el mapa público. |
| hubo_atencion_medica | BOOLEAN | No |  | false | [SENSIBLE] Si la persona recibió atención médica (dato de salud). |

**Restricciones:**

- `ck_incidentes_atencion`: `CHECK (((NOT hubo_atencion_medica) OR ((tipo_dano)::text = ANY ((ARRAY['LESION'::character varying, 'AMBOS'::character varying])::text[]))))`
- `ck_incidentes_descripcion`: `CHECK ((length(TRIM(BOTH FROM descripcion_dano)) >= 10))`
- `ck_incidentes_tipo_dano`: `CHECK (((tipo_dano)::text = ANY ((ARRAY['LESION'::character varying, 'DANO_MATERIAL'::character varying, 'AMBOS'::character varying])::text[])))`
- `ck_incidentes_tipo_reporte`: `CHECK (((tipo_reporte)::text = 'INCIDENTE'::text))`
- `fk_incidentes_entidad`: `FOREIGN KEY (id_entidad) REFERENCES entidades_publicas(id_entidad) ON DELETE RESTRICT`
- `fk_incidentes_reporte`: `FOREIGN KEY (id_reporte, tipo_reporte) REFERENCES reportes(id_reporte, tipo_reporte) ON DELETE CASCADE`
- `uq_incidentes_reporte`: `UNIQUE (id_reporte)`

**Índices adicionales:**

- `ix_incidentes_entidad`: `btree (id_entidad)`

## testigos

Personas que presenciaron el incidente (fortalecen el caso).

| Columna | Tipo | Nulo | Claves | Defecto | Significado de negocio |
|---|---|---|---|---|---|
| id_testigo | INTEGER | No | PK | identidad | Identificador único del testigo. |
| id_incidente | INTEGER | No | FK, UQ |  | Incidente que presenció. |
| nombre | VARCHAR(120) | No | UQ |  | [PERSONAL] Nombre del testigo. Protección: solo visible para operador y abogado del caso. |
| contacto | VARCHAR(150) | No |  |  | [PERSONAL] Teléfono o correo del testigo. Protección: solo visible para operador y abogado del caso. |

**Restricciones:**

- `fk_testigos_incidente`: `FOREIGN KEY (id_incidente) REFERENCES incidentes(id_incidente) ON DELETE CASCADE`
- `uq_testigos_incidente_nombre`: `UNIQUE (id_incidente, nombre)`

## casos

Expediente legal creado a partir de un incidente viable (1:1 con incidentes).

| Columna | Tipo | Nulo | Claves | Defecto | Significado de negocio |
|---|---|---|---|---|---|
| id_caso | INTEGER | No | PK | identidad | Identificador único del caso. |
| id_incidente | INTEGER | No | FK, UQ |  | Incidente que origina el caso (UNIQUE: un incidente, un caso). |
| id_estado_caso | INTEGER | No | FK |  | Estado actual del caso. |
| id_operador | INTEGER | No | FK |  | Operador que validó el incidente del que nace el caso. |
| fecha_creacion | TIMESTAMPTZ | No |  | now() | Momento en que se abrió el caso. |
| fecha_cierre | TIMESTAMPTZ | Sí |  |  | Momento de cierre (nulo mientras está abierto). |
| observacion | TEXT | Sí |  |  | Notas internas del operador sobre el caso. |
| fecha_consentimiento | TIMESTAMPTZ | Sí |  |  | Momento en que el ciudadano autorizó mostrar un resumen anónimo de su caso a los abogados aliados. |

**Restricciones:**

- `ck_casos_fechas`: `CHECK (((fecha_cierre IS NULL) OR (fecha_cierre >= fecha_creacion)))`
- `fk_casos_estado`: `FOREIGN KEY (id_estado_caso) REFERENCES estados_caso(id_estado_caso) ON DELETE RESTRICT`
- `fk_casos_incidente`: `FOREIGN KEY (id_incidente) REFERENCES incidentes(id_incidente) ON DELETE RESTRICT`
- `fk_casos_operador`: `FOREIGN KEY (id_operador) REFERENCES usuarios(id_usuario) ON DELETE RESTRICT`
- `uq_casos_incidente`: `UNIQUE (id_incidente)`

**Índices adicionales:**

- `ix_casos_estado`: `btree (id_estado_caso)`
- `ix_casos_operador`: `btree (id_operador)`

## remisiones

Envío de un caso a un abogado; unidad de facturación del negocio (cuota fija por caso remitido).

| Columna | Tipo | Nulo | Claves | Defecto | Significado de negocio |
|---|---|---|---|---|---|
| id_remision | INTEGER | No | PK | identidad | Identificador único de la remisión. |
| id_caso | INTEGER | No | FK, UQ |  | Caso remitido. |
| id_abogado | INTEGER | No | FK, UQ |  | Abogado que recibe el caso (no se repite el mismo abogado para el mismo caso). |
| fecha_envio | TIMESTAMPTZ | No |  | now() | Momento del envío. |
| estado_remision | VARCHAR(10) | No |  | 'ENVIADA' | ENVIADA, ACEPTADA, RECHAZADA o VENCIDA. Solo una ACEPTADA por caso. |
| fecha_respuesta | TIMESTAMPTZ | Sí |  |  | Momento en que el abogado aceptó o rechazó (nulo si no ha respondido). |
| motivo_rechazo | TEXT | Sí |  |  | Motivo dado por el abogado al rechazar. |
| valor_cobrado | NUMERIC(12,2) | No |  |  | Valor en pesos colombianos (COP) cobrado al abogado por el caso remitido. |

**Restricciones:**

- `ck_remisiones_estado`: `CHECK (((estado_remision)::text = ANY ((ARRAY['ENVIADA'::character varying, 'ACEPTADA'::character varying, 'RECHAZADA'::character varying, 'VENCIDA'::character varying])::text[])))`
- `ck_remisiones_motivo`: `CHECK (((motivo_rechazo IS NULL) OR ((estado_remision)::text = 'RECHAZADA'::text)))`
- `ck_remisiones_respuesta`: `CHECK (((((estado_remision)::text = ANY ((ARRAY['ENVIADA'::character varying, 'VENCIDA'::character varying])::text[])) AND (fecha_respuesta IS NULL)) OR (((estado_remision)::text = ANY ((ARRAY['ACEPTADA'::character varying, 'RECHAZADA'::character varying])::text[])) AND (fecha_respuesta IS NOT NULL) AND (fecha_respuesta >= fecha_envio))))`
- `ck_remisiones_valor`: `CHECK ((valor_cobrado >= (0)::numeric))`
- `fk_remisiones_abogado`: `FOREIGN KEY (id_abogado) REFERENCES abogados(id_abogado) ON DELETE RESTRICT`
- `fk_remisiones_caso`: `FOREIGN KEY (id_caso) REFERENCES casos(id_caso) ON DELETE RESTRICT`
- `uq_remisiones_caso_abogado`: `UNIQUE (id_caso, id_abogado)`

**Índices adicionales:**

- `ix_remisiones_abogado`: `btree (id_abogado)`
- `uq_remisiones_una_aceptada`: `btree (id_caso) WHERE ((estado_remision)::text = 'ACEPTADA'::text)`

## versiones_indice

Versiones de configuración de cada índice (IRU o IVC): umbrales de nivel y cuál está vigente.

| Columna | Tipo | Nulo | Claves | Defecto | Significado de negocio |
|---|---|---|---|---|---|
| id_version | INTEGER | No | PK | identidad | Identificador único de la versión. |
| nombre_indice | VARCHAR(3) | No | UQ |  | IRU (Índice de Riesgo Urbano) o IVC (Índice de Viabilidad del Caso). |
| numero_version | SMALLINT | No | UQ |  | Número de versión, consecutivo por índice. |
| descripcion | TEXT | No |  |  | Qué cambió en esta versión y de dónde salen los pesos. |
| umbral_medio | NUMERIC(5,2) | No |  | 25 | Desde este puntaje el nivel es MEDIO (preliminar, por calibrar). |
| umbral_alto | NUMERIC(5,2) | No |  | 50 | Desde este puntaje el nivel es ALTO (preliminar, por calibrar). |
| umbral_critico | NUMERIC(5,2) | No |  | 75 | Desde este puntaje el nivel es CRÍTICO (preliminar, por calibrar). |
| vigente | BOOLEAN | No |  | false | true = versión usada en los cálculos nuevos. Máximo una por índice. |
| fecha_creacion | TIMESTAMPTZ | No |  | now() | Momento de creación de la versión. |

**Restricciones:**

- `ck_versiones_nombre`: `CHECK (((nombre_indice)::text = ANY ((ARRAY['IRU'::character varying, 'IVC'::character varying])::text[])))`
- `ck_versiones_numero`: `CHECK ((numero_version >= 1))`
- `ck_versiones_umbrales`: `CHECK ((((0)::numeric < umbral_medio) AND (umbral_medio < umbral_alto) AND (umbral_alto < umbral_critico) AND (umbral_critico <= (100)::numeric)))`
- `uq_versiones_indice`: `UNIQUE (nombre_indice, numero_version)`

**Índices adicionales:**

- `uq_versiones_una_vigente`: `btree (nombre_indice) WHERE vigente`

## parametros_indice

Peso de cada variable en una versión de índice (configurable y versionado).

| Columna | Tipo | Nulo | Claves | Defecto | Significado de negocio |
|---|---|---|---|---|---|
| id_version | INTEGER | No | FK, PK |  | Versión del índice a la que pertenece el peso. |
| variable | VARCHAR(40) | No | PK |  | Nombre técnico de la variable (snake_case), p. ej. antecedentes_previos. |
| peso | NUMERIC(5,4) | No |  |  | Peso de la variable, en (0, 1]. Los pesos de una versión deben sumar 1. |
| descripcion | TEXT | No |  |  | Qué mide la variable y cómo se normaliza a 0-100. |

**Restricciones:**

- `ck_parametros_peso`: `CHECK (((peso > (0)::numeric) AND (peso <= (1)::numeric)))`
- `ck_parametros_variable`: `CHECK (((variable)::text ~ '^[a-z_]+$'::text))`
- `fk_parametros_version`: `FOREIGN KEY (id_version) REFERENCES versiones_indice(id_version) ON DELETE CASCADE`

## calculos_indice

Historial de cálculos de IRU/IVC por reporte, con el desglose de razones.

| Columna | Tipo | Nulo | Claves | Defecto | Significado de negocio |
|---|---|---|---|---|---|
| id_calculo | INTEGER | No | PK | identidad | Identificador único del cálculo. |
| id_reporte | INTEGER | No | FK |  | Reporte calculado (IRU para condiciones e incidentes; IVC solo para incidentes). |
| id_version | INTEGER | No | FK |  | Versión de parámetros usada (de ahí sale si es IRU o IVC). |
| valor | NUMERIC(5,2) | No |  |  | Puntaje total 0-100. El nivel (Bajo/Medio/Alto/Crítico) se deriva con los umbrales de la versión, no se almacena. |
| desglose | JSONB | No |  |  | JSON {variable: {valor_normalizado, peso, aporte}} para explicar el resultado. |
| fecha_calculo | TIMESTAMPTZ | No |  | now() | Momento del cálculo. |

**Restricciones:**

- `ck_calculos_desglose`: `CHECK ((jsonb_typeof(desglose) = 'object'::text))`
- `ck_calculos_valor`: `CHECK (((valor >= (0)::numeric) AND (valor <= (100)::numeric)))`
- `fk_calculos_reporte`: `FOREIGN KEY (id_reporte) REFERENCES reportes(id_reporte) ON DELETE CASCADE`
- `fk_calculos_version`: `FOREIGN KEY (id_version) REFERENCES versiones_indice(id_version) ON DELETE RESTRICT`

**Índices adicionales:**

- `ix_calculos_reporte`: `btree (id_reporte, fecha_calculo DESC)`
- `ix_calculos_version`: `btree (id_version)`

## postulaciones

Interés de un abogado en tomar un caso. El ciudadano compara las postulaciones y elige una.

| Columna | Tipo | Nulo | Claves | Defecto | Significado de negocio |
|---|---|---|---|---|---|
| id_postulacion | INTEGER | No | PK | identidad | Identificador único de la postulación. |
| id_caso | INTEGER | No | FK, UQ |  | Caso al que se postula el abogado. |
| id_abogado | INTEGER | No | FK, UQ |  | Abogado que se postula (no puede postularse dos veces al mismo caso). |
| mensaje | TEXT | No |  |  | Por qué el abogado quiere el caso y cómo lo llevaría (mínimo 20 caracteres). |
| estado | VARCHAR(12) | No |  | 'PENDIENTE' | PENDIENTE, ELEGIDA (el ciudadano lo escogió), NO_ELEGIDA (escogió a otro) o RETIRADA (el abogado desistió). |
| fecha_postulacion | TIMESTAMPTZ | No |  | now() | Momento de la postulación. |
| fecha_resolucion | TIMESTAMPTZ | Sí |  |  | Momento en que la postulación dejó de estar pendiente. |

**Restricciones:**

- `ck_postulaciones_estado`: `CHECK (((estado)::text = ANY ((ARRAY['PENDIENTE'::character varying, 'ELEGIDA'::character varying, 'NO_ELEGIDA'::character varying, 'RETIRADA'::character varying])::text[])))`
- `ck_postulaciones_mensaje`: `CHECK ((length(TRIM(BOTH FROM mensaje)) >= 20))`
- `ck_postulaciones_resolucion`: `CHECK (((((estado)::text = 'PENDIENTE'::text) AND (fecha_resolucion IS NULL)) OR (((estado)::text <> 'PENDIENTE'::text) AND (fecha_resolucion IS NOT NULL) AND (fecha_resolucion >= fecha_postulacion))))`
- `fk_postulaciones_abogado`: `FOREIGN KEY (id_abogado) REFERENCES abogados(id_abogado) ON DELETE RESTRICT`
- `fk_postulaciones_caso`: `FOREIGN KEY (id_caso) REFERENCES casos(id_caso) ON DELETE CASCADE`
- `uq_postulaciones_caso_abogado`: `UNIQUE (id_caso, id_abogado)`

**Índices adicionales:**

- `ix_postulaciones_abogado`: `btree (id_abogado)`
- `uq_postulaciones_una_elegida`: `btree (id_caso) WHERE ((estado)::text = 'ELEGIDA'::text)`

## tokens_recuperacion

Enlaces de un solo uso para restablecer la contraseña (vencen a los 30 minutos).

| Columna | Tipo | Nulo | Claves | Defecto | Significado de negocio |
|---|---|---|---|---|---|
| id_token | INTEGER | No | PK | identidad | Identificador único del token. |
| id_usuario | INTEGER | No | FK |  | Usuario que pidió restablecer su contraseña. |
| hash_token | CHAR(64) | No | UQ |  | SHA-256 del token enviado por correo. Protección: el token en claro nunca se guarda. |
| fecha_creacion | TIMESTAMPTZ | No |  | now() | Momento de la solicitud. |
| fecha_expiracion | TIMESTAMPTZ | No |  |  | Después de este momento el enlace ya no sirve. |
| fecha_uso | TIMESTAMPTZ | Sí |  |  | Momento en que se usó (nulo si no se ha usado). Un token usado no vuelve a servir. |

**Restricciones:**

- `ck_tokens_hash`: `CHECK ((hash_token ~ '^[0-9a-f]{64}$'::text))`
- `ck_tokens_uso`: `CHECK (((fecha_uso IS NULL) OR ((fecha_uso >= fecha_creacion) AND (fecha_uso <= fecha_expiracion))))`
- `ck_tokens_vigencia`: `CHECK ((fecha_expiracion > fecha_creacion))`
- `fk_tokens_usuario`: `FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE`
- `uq_tokens_hash`: `UNIQUE (hash_token)`

**Índices adicionales:**

- `ix_tokens_usuario`: `btree (id_usuario)`

## calificaciones

Calificación que el ciudadano da al abogado que llevó su caso (una por caso).

| Columna | Tipo | Nulo | Claves | Defecto | Significado de negocio |
|---|---|---|---|---|---|
| id_calificacion | INTEGER | No | PK | identidad | Identificador único de la calificación. |
| id_caso | INTEGER | No | FK, UQ |  | Caso calificado (UNIQUE: una calificación por caso). El autor es el dueño del caso. |
| id_abogado | INTEGER | No | FK |  | Abogado calificado: el que el ciudadano eligió para el caso. |
| puntaje | SMALLINT | No |  |  | De 1 (muy malo) a 5 (excelente) estrellas. |
| comentario | TEXT | Sí |  |  | Por qué de la calificación (opcional). Se muestra a otros ciudadanos sin el nombre del autor. |
| fecha | TIMESTAMPTZ | No |  | now() | Momento de la calificación. |

**Restricciones:**

- `ck_calificaciones_comentario`: `CHECK (((comentario IS NULL) OR ((length(TRIM(BOTH FROM comentario)) >= 3) AND (length(TRIM(BOTH FROM comentario)) <= 1000))))`
- `ck_calificaciones_puntaje`: `CHECK (((puntaje >= 1) AND (puntaje <= 5)))`
- `fk_calificaciones_abogado`: `FOREIGN KEY (id_abogado) REFERENCES abogados(id_abogado) ON DELETE RESTRICT`
- `fk_calificaciones_caso`: `FOREIGN KEY (id_caso) REFERENCES casos(id_caso) ON DELETE RESTRICT`
- `uq_calificaciones_caso`: `UNIQUE (id_caso)`

**Índices adicionales:**

- `ix_calificaciones_abogado`: `btree (id_abogado, fecha DESC)`

