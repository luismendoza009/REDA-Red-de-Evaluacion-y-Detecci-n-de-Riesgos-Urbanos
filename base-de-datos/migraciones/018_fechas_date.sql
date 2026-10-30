-- 018_fechas_date.sql
-- La rubrica exige fechas DATE. La conversion conserva el dia de Colombia y pierde la hora.
SET LOCAL timezone = 'America/Bogota';

DROP VIEW v_resumen_casos;
DROP VIEW v_mapa_publico;

ALTER TABLE usuarios
    ALTER COLUMN fecha_registro DROP DEFAULT,
    ALTER COLUMN fecha_consentimiento DROP DEFAULT;
ALTER TABLE reportes ALTER COLUMN fecha_reporte DROP DEFAULT;
ALTER TABLE evidencias ALTER COLUMN fecha_carga DROP DEFAULT;
ALTER TABLE evaluaciones ALTER COLUMN fecha_evaluacion DROP DEFAULT;
ALTER TABLE historial_estados ALTER COLUMN fecha_cambio DROP DEFAULT;
ALTER TABLE reportes_relacionados ALTER COLUMN fecha_vinculo DROP DEFAULT;
ALTER TABLE casos ALTER COLUMN fecha_creacion DROP DEFAULT;
ALTER TABLE remisiones ALTER COLUMN fecha_envio DROP DEFAULT;
ALTER TABLE versiones_indice ALTER COLUMN fecha_creacion DROP DEFAULT;
ALTER TABLE calculos_indice ALTER COLUMN fecha_calculo DROP DEFAULT;
ALTER TABLE postulaciones ALTER COLUMN fecha_postulacion DROP DEFAULT;
ALTER TABLE tokens_recuperacion ALTER COLUMN fecha_creacion DROP DEFAULT;
ALTER TABLE calificaciones ALTER COLUMN fecha DROP DEFAULT;
ALTER TABLE notificaciones ALTER COLUMN fecha_creacion DROP DEFAULT;

ALTER TABLE usuarios
    ALTER COLUMN fecha_registro TYPE DATE USING fecha_registro::date,
    ALTER COLUMN fecha_registro SET DEFAULT CURRENT_DATE,
    ALTER COLUMN fecha_consentimiento TYPE DATE USING fecha_consentimiento::date,
    ALTER COLUMN fecha_consentimiento SET DEFAULT CURRENT_DATE,
    ALTER COLUMN fecha_foto_perfil TYPE DATE USING fecha_foto_perfil::date;

ALTER TABLE reportes
    ALTER COLUMN fecha_reporte TYPE DATE USING fecha_reporte::date,
    ALTER COLUMN fecha_reporte SET DEFAULT CURRENT_DATE;

ALTER TABLE evidencias
    ALTER COLUMN fecha_captura TYPE DATE USING fecha_captura::date,
    ALTER COLUMN fecha_carga TYPE DATE USING fecha_carga::date,
    ALTER COLUMN fecha_carga SET DEFAULT CURRENT_DATE;

ALTER TABLE evaluaciones
    ALTER COLUMN fecha_evaluacion TYPE DATE USING fecha_evaluacion::date,
    ALTER COLUMN fecha_evaluacion SET DEFAULT CURRENT_DATE;

ALTER TABLE historial_estados
    ALTER COLUMN fecha_cambio TYPE DATE USING fecha_cambio::date,
    ALTER COLUMN fecha_cambio SET DEFAULT CURRENT_DATE;

ALTER TABLE reportes_relacionados
    ALTER COLUMN fecha_vinculo TYPE DATE USING fecha_vinculo::date,
    ALTER COLUMN fecha_vinculo SET DEFAULT CURRENT_DATE;

ALTER TABLE incidentes
    ALTER COLUMN fecha_hora_hecho TYPE DATE USING fecha_hora_hecho::date;
ALTER TABLE incidentes RENAME COLUMN fecha_hora_hecho TO fecha_hecho;

ALTER TABLE casos
    ALTER COLUMN fecha_creacion TYPE DATE USING fecha_creacion::date,
    ALTER COLUMN fecha_creacion SET DEFAULT CURRENT_DATE,
    ALTER COLUMN fecha_cierre TYPE DATE USING fecha_cierre::date,
    ALTER COLUMN fecha_consentimiento TYPE DATE USING fecha_consentimiento::date;

ALTER TABLE remisiones
    ALTER COLUMN fecha_envio TYPE DATE USING fecha_envio::date,
    ALTER COLUMN fecha_envio SET DEFAULT CURRENT_DATE,
    ALTER COLUMN fecha_respuesta TYPE DATE USING fecha_respuesta::date;

ALTER TABLE versiones_indice
    ALTER COLUMN fecha_creacion TYPE DATE USING fecha_creacion::date,
    ALTER COLUMN fecha_creacion SET DEFAULT CURRENT_DATE;

ALTER TABLE calculos_indice
    ALTER COLUMN fecha_calculo TYPE DATE USING fecha_calculo::date,
    ALTER COLUMN fecha_calculo SET DEFAULT CURRENT_DATE;

ALTER TABLE postulaciones
    ALTER COLUMN fecha_postulacion TYPE DATE USING fecha_postulacion::date,
    ALTER COLUMN fecha_postulacion SET DEFAULT CURRENT_DATE,
    ALTER COLUMN fecha_resolucion TYPE DATE USING fecha_resolucion::date;

ALTER TABLE tokens_recuperacion
    ALTER COLUMN fecha_creacion TYPE DATE USING fecha_creacion::date,
    ALTER COLUMN fecha_creacion SET DEFAULT CURRENT_DATE,
    ALTER COLUMN fecha_expiracion TYPE DATE USING fecha_expiracion::date,
    ALTER COLUMN fecha_uso TYPE DATE USING fecha_uso::date;

ALTER TABLE calificaciones
    ALTER COLUMN fecha TYPE DATE USING fecha::date,
    ALTER COLUMN fecha SET DEFAULT CURRENT_DATE;

ALTER TABLE bloqueos_login
    ALTER COLUMN bloqueado_hasta TYPE DATE USING bloqueado_hasta::date;

ALTER TABLE notificaciones
    ALTER COLUMN fecha_creacion TYPE DATE USING fecha_creacion::date,
    ALTER COLUMN fecha_creacion SET DEFAULT CURRENT_DATE,
    ALTER COLUMN fecha_lectura TYPE DATE USING fecha_lectura::date;

COMMENT ON COLUMN usuarios.fecha_registro IS 'Fecha en que se creó la cuenta.';
COMMENT ON COLUMN usuarios.fecha_consentimiento IS 'Fecha en que el usuario aceptó la política de tratamiento de datos (Ley 1581 de 2012).';
COMMENT ON COLUMN usuarios.fecha_foto_perfil IS 'Fecha en que se cargó la foto de perfil actual.';
COMMENT ON COLUMN reportes.fecha_reporte IS 'Día en que se registró el reporte.';
COMMENT ON COLUMN evidencias.fecha_captura IS 'Fecha de toma de la foto según sus metadatos (si existen). [PERSONAL] Los metadatos EXIF pueden revelar ubicación.';
COMMENT ON COLUMN evidencias.fecha_carga IS 'Día en que se subió la foto a la plataforma.';
COMMENT ON COLUMN evaluaciones.fecha_evaluacion IS 'Fecha de la evaluación.';
COMMENT ON COLUMN historial_estados.fecha_cambio IS 'Día del cambio de estado.';
COMMENT ON COLUMN reportes_relacionados.fecha_vinculo IS 'Día en que se creó el vínculo.';
COMMENT ON COLUMN incidentes.fecha_hecho IS 'Día en que ocurrió el daño (puede ser anterior al reporte).';
COMMENT ON COLUMN casos.fecha_creacion IS 'Día en que se abrió el caso.';
COMMENT ON COLUMN casos.fecha_cierre IS 'Día de cierre (nulo mientras está abierto).';
COMMENT ON COLUMN casos.fecha_consentimiento IS 'Día en que el ciudadano autorizó mostrar un resumen anónimo de su caso a los abogados aliados.';
COMMENT ON COLUMN remisiones.fecha_envio IS 'Día del envío.';
COMMENT ON COLUMN remisiones.fecha_respuesta IS 'Día en que el abogado aceptó o rechazó (nulo si no ha respondido).';
COMMENT ON COLUMN versiones_indice.fecha_creacion IS 'Fecha de creación de la versión.';
COMMENT ON COLUMN calculos_indice.fecha_calculo IS 'Fecha del cálculo.';
COMMENT ON COLUMN postulaciones.fecha_postulacion IS 'Fecha de la postulación.';
COMMENT ON COLUMN postulaciones.fecha_resolucion IS 'Fecha en que la postulación dejó de estar pendiente.';
COMMENT ON COLUMN tokens_recuperacion.fecha_creacion IS 'Fecha de la solicitud.';
COMMENT ON COLUMN tokens_recuperacion.fecha_expiracion IS 'El enlace deja de servir al comenzar este día; vence el día siguiente a su solicitud.';
COMMENT ON COLUMN tokens_recuperacion.fecha_uso IS 'Fecha en que se usó (nulo si no se ha usado). Un token usado no vuelve a servir.';
COMMENT ON TABLE tokens_recuperacion IS 'Enlaces de un solo uso para restablecer la contraseña (vencen al comenzar el día siguiente).';
COMMENT ON COLUMN calificaciones.fecha IS 'Fecha de la calificación.';
COMMENT ON COLUMN bloqueos_login.bloqueado_hasta IS 'Último día de un bloqueo temporal; nulo si no hay bloqueo temporal.';
COMMENT ON COLUMN notificaciones.fecha_creacion IS 'Día en que se generó la notificación.';
COMMENT ON COLUMN notificaciones.fecha_lectura IS 'Día en que el ciudadano la marcó como leída; NULL indica pendiente.';

CREATE VIEW v_mapa_publico AS
SELECT r.id_reporte,
       r.tipo_reporte,
       c.nombre                     AS categoria,
       z.nombre                     AS zona,
       e.nombre                     AS estado,
       round(r.latitud, 4)          AS latitud,
       round(r.longitud, 4)         AS longitud,
       r.fecha_reporte              AS fecha,
       r.nivel_gravedad,
       (SELECT count(*) FROM evidencias ev WHERE ev.id_reporte = r.id_reporte) AS num_evidencias,
       r.descripcion,
       CASE
           WHEN ec.nombre IN ('Cerrado', 'Rechazado') OR e.nombre = 'Solucionado' THEN 'CERRADO'
           WHEN EXISTS (SELECT 1 FROM remisiones rm
                        WHERE rm.id_caso = ca.id_caso AND rm.estado_remision = 'ACEPTADA') THEN 'CON_ABOGADO'
           ELSE 'ABIERTO'
       END                          AS situacion,
       ec.nombre                    AS estado_caso
FROM reportes r
JOIN categorias c ON c.id_categoria = r.id_categoria
JOIN zonas      z ON z.id_zona      = r.id_zona
JOIN estados    e ON e.id_estado    = r.id_estado
LEFT JOIN incidentes   i  ON i.id_reporte      = r.id_reporte
LEFT JOIN casos        ca ON ca.id_incidente   = i.id_incidente
LEFT JOIN estados_caso ec ON ec.id_estado_caso = ca.id_estado_caso
WHERE e.nombre <> 'Rechazado';

COMMENT ON VIEW v_mapa_publico IS 'Reportes visibles en el mapa público, sin datos personales ni del daño; excluye rechazados. Incluye la situación pública del caso (ABIERTO, CON_ABOGADO, CERRADO).';

CREATE VIEW v_resumen_casos AS
WITH antecedentes AS (
    SELECT rr.id_reporte,
           count(*)               AS num_antecedentes,
           min(ra.fecha_reporte)  AS primer_antecedente
    FROM reportes_relacionados rr
    JOIN reportes ra ON ra.id_reporte = rr.id_reporte_relacionado
    WHERE rr.tipo_relacion = 'ANTECEDENTE'
    GROUP BY rr.id_reporte
),
remis AS (
    SELECT id_caso,
           count(*)                                        AS num_remisiones,
           count(*) FILTER (WHERE estado_remision = 'ACEPTADA') AS aceptadas,
           sum(valor_cobrado)                              AS total_cobrado
    FROM remisiones
    GROUP BY id_caso
)
SELECT ca.id_caso,
       ec.nombre                              AS estado_caso,
       r.id_reporte,
       c.nombre                               AS categoria,
       z.nombre                               AS zona,
       i.fecha_hecho,
       i.tipo_dano,
       i.hubo_atencion_medica,
       ep.nombre                              AS entidad_responsable,
       coalesce(a.num_antecedentes, 0)        AS num_antecedentes,
       a.primer_antecedente,
       CASE WHEN a.primer_antecedente IS NOT NULL
            THEN (i.fecha_hecho - a.primer_antecedente) END AS dias_conocimiento_previo,
       (SELECT count(*) FROM testigos t WHERE t.id_incidente = i.id_incidente) AS num_testigos,
       coalesce(rm.num_remisiones, 0)         AS num_remisiones,
       coalesce(rm.aceptadas, 0) > 0          AS aceptado,
       coalesce(rm.total_cobrado, 0)          AS total_cobrado,
       ca.fecha_creacion
FROM casos ca
JOIN estados_caso ec        ON ec.id_estado_caso = ca.id_estado_caso
JOIN incidentes i           ON i.id_incidente    = ca.id_incidente
JOIN reportes r             ON r.id_reporte      = i.id_reporte
JOIN categorias c           ON c.id_categoria   = r.id_categoria
JOIN zonas z                ON z.id_zona         = r.id_zona
LEFT JOIN entidades_publicas ep ON ep.id_entidad = i.id_entidad
LEFT JOIN antecedentes a    ON a.id_reporte      = r.id_reporte
LEFT JOIN remis rm          ON rm.id_caso        = ca.id_caso;

COMMENT ON VIEW v_resumen_casos IS 'Un renglón por caso: incidente, entidad, antecedentes ("la entidad ya sabía desde..."), testigos y remisiones.';
