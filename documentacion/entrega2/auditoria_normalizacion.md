# Auditoría de normalización — REDA (Entrega 2)

Método: para cada tabla se listan las dependencias funcionales (DF) no triviales, las claves
candidatas y la verificación de forma normal. Recordatorio de criterios:

- **1FN**: atributos atómicos, sin grupos repetidos.
- **2FN**: ningún atributo no primo depende de una *parte* de una clave candidata.
- **3FN**: ningún atributo no primo depende transitivamente de una clave (X → Y → A, con Y no clave).
- **FNBC (BCNF)**: para toda DF X → A no trivial, X es superclave.

Todas las tablas cumplen 1FN: no hay listas ni atributos multivaluados (las fotos, testigos,
cambios de estado y remisiones están en tablas propias). El único atributo compuesto,
`calculos_indice.desglose` (JSONB), se trata como un valor atómico: es un resultado que se
guarda y se muestra entero, nunca se consulta ni se actualiza por partes (ver excepción E4).

## Resumen

| # | Tabla | Claves candidatas | FN alcanzada | Observación |
|---|---|---|---|---|
| 1 | roles | {id_rol}, {nombre} | FNBC | |
| 2 | categorias | {id_categoria}, {nombre} | FNBC | |
| 3 | zonas | {id_zona}, {nombre, ciudad} | FNBC | |
| 4 | estados | {id_estado}, {nombre} | FNBC | |
| 5 | estados_caso | {id_estado_caso}, {nombre} | FNBC | |
| 6 | entidades_publicas | {id_entidad}, {nombre} | FNBC | |
| 7 | usuarios | {id_usuario}, {correo} | FNBC | |
| 8 | abogados | {id_abogado}, {id_usuario}, {tarjeta_profesional} | FNBC | |
| 9 | reportes | {id_reporte} | FNBC* | *E1: id_zona frente a coordenadas; E2: id_estado redundante con el historial |
| 10 | evidencias | {id_evidencia}, {ruta_imagen}, {hash_sha256} | FNBC | |
| 11 | evaluaciones | {id_evaluacion} | FNBC | |
| 12 | historial_estados | {id_historial} | FNBC | E2: redundancia entre filas, a propósito |
| 13 | reportes_relacionados | {id_reporte, id_reporte_relacionado} | FNBC | |
| 14 | incidentes | {id_incidente}, {id_reporte} | 3FN | E3: columna constante `tipo_reporte` |
| 15 | testigos | {id_testigo}, {id_incidente, nombre} | FNBC | |
| 16 | casos | {id_caso}, {id_incidente} | FNBC | |
| 17 | remisiones | {id_remision}, {id_caso, id_abogado} | FNBC | |
| 18 | versiones_indice | {id_version}, {nombre_indice, numero_version} | FNBC | Nació de corregir una violación de 2FN |
| 19 | parametros_indice | {id_version, variable} | FNBC | |
| 20 | calculos_indice | {id_calculo} | FNBC | E4: JSONB del desglose |

## Detalle por tabla

**1. roles** — id_rol → nombre; nombre → id_rol. Los dos determinantes son claves. FNBC.

**2. categorias** — id_categoria → nombre, descripcion; nombre → id_categoria, descripcion. FNBC.

**3. zonas** — id_zona → nombre, tipo, ciudad, nivel_trafico, centros_educativos, centros_salud;
{nombre, ciudad} → id_zona, (resto). Un mismo nombre de barrio puede existir en dos ciudades, por eso
la clave natural incluye la ciudad. Las variables de contexto (tráfico, colegios, salud) describen la
zona y dependen solo de ella. FNBC.

**4. estados** y **5. estados_caso** — id → nombre; nombre → id. FNBC. Se mantienen separados porque son
dominios distintos (ciclo de la falla vs. ciclo del caso legal); unirlos en un catálogo con una columna
"ámbito" obligaría a validar con trigger que un reporte no tome un estado de caso.

**6. entidades_publicas** — id_entidad → nombre, tipo, ciudad; nombre → id_entidad, tipo, ciudad. FNBC.

**7. usuarios** — id_usuario → nombre, correo, password_hash, id_rol, fecha_registro, activo,
fecha_consentimiento; correo → id_usuario (y lo demás). El nombre del rol no está aquí (está en roles),
lo que evita la dependencia transitiva id_usuario → id_rol → nombre_rol. FNBC.

**8. abogados** — id_abogado → id_usuario, tarjeta_profesional, especialidad, ciudad, activo;
id_usuario → id_abogado…; tarjeta_profesional → id_abogado… Tres claves candidatas. El nombre y
el correo del abogado no se repiten aquí: se obtienen de usuarios. FNBC.

**9. reportes** — id_reporte → id_usuario, id_categoria, id_zona, id_estado, tipo_reporte, descripcion,
latitud, longitud, fecha_reporte, nivel_gravedad. Los nombres de categoría, zona y estado no se repiten
(están en sus catálogos). FNBC, con dos observaciones:

- **E1 — id_zona y coordenadas.** En el mundo real un punto (latitud, longitud) cae en un único barrio,
  es decir, habría una DF {latitud, longitud} → id_zona, y por tanto una dependencia transitiva.
  Hoy el sistema **no tiene los polígonos de los barrios**, así que no puede deducir la zona a partir del
  punto: la zona la declara el ciudadano o la corrige el operador, y es un dato propio del reporte.
  Cuando se incorporen polígonos (PostGIS, trabajo futuro) id_zona pasará a calcularse y deberá
  eliminarse de la tabla o mantenerse con una restricción que lo verifique.
- **E2 — id_estado redundante con el historial.** El estado actual también podría obtenerse como el
  `id_estado_nuevo` del último registro de historial_estados. Es una redundancia **entre tablas**
  (no una DF dentro de reportes, así que no rompe la FNBC), mantenida a propósito: el estado actual se
  consulta en casi todas las pantallas y calcularlo cada vez exige ordenar el historial. El riesgo
  (que ambos se desincronicen) se controla con la comprobación 1 de `sql/pruebas/verificar_semilla.sql`
  y, en la Entrega 3, con una transacción o un trigger que actualice ambos a la vez.

**10. evidencias** — id_evidencia → (todo); ruta_imagen → (todo); hash_sha256 → (todo). El tipo y el
tamaño dependen del archivo, y el archivo está identificado por su hash, que es clave candidata, así que
no hay transitividad. FNBC.

**11. evaluaciones** — id_evaluacion → id_reporte, id_usuario, gravedad, impacto, frecuencia,
fecha_evaluacion. Un reporte puede tener varias evaluaciones (reevaluaciones), por eso id_reporte no es
clave. FNBC.

**12. historial_estados** — id_historial → id_reporte, id_estado_anterior, id_estado_nuevo, id_usuario,
fecha_cambio, observacion. FNBC. *Redundancia entre filas:* id_estado_anterior coincide con el
id_estado_nuevo del registro anterior del mismo reporte. No es una DF dentro de una fila sino una regla
entre filas. Se guarda a propósito, porque cada fila de una bitácora debe poder leerse sola, sin
reconstruir la cadena. La comprobación 2 de `verificar_semilla.sql` verifica que la cadena no tenga saltos.

**13. reportes_relacionados** — {id_reporte, id_reporte_relacionado} → tipo_relacion, origen,
fecha_vinculo. FNBC. La distancia entre los dos reportes **no** se guarda: se deriva de las coordenadas
(guardarla sería un dato derivado que podría quedar desactualizado si se corrige una ubicación).

**14. incidentes** — id_incidente → (todo); id_reporte → (todo). Las demás columnas (fecha del hecho,
tipo y descripción del daño, atención médica, entidad) dependen del incidente y solo de él. La entidad
responsable **no** depende funcionalmente de la categoría: la categoría sugiere una entidad por defecto,
pero el operador puede atribuir la falla a otra (p. ej. una vía nacional), así que no es una DF.

- **E3 — columna constante tipo_reporte.** Siempre vale 'INCIDENTE', es decir, ∅ → tipo_reporte. Formalmente
  es una DF cuyo determinante (el conjunto vacío) no es superclave, así que la tabla queda en 3FN pero no
  en FNBC estricta. **Excepción justificada:** la columna existe solo para la FK compuesta
  (id_reporte, tipo_reporte) → reportes, que garantiza **de forma declarativa** que un incidente nunca
  cuelga de un reporte de condición. La alternativa, un trigger, es menos transparente y más fácil de
  saltarse. Como el CHECK fija el valor, la redundancia no puede generar anomalías de actualización.

**15. testigos** — id_testigo → id_incidente, nombre, contacto; {id_incidente, nombre} → id_testigo,
contacto. FNBC.

**16. casos** — id_caso → id_incidente, id_estado_caso, id_operador, fecha_creacion, fecha_cierre,
observacion; id_incidente → id_caso… FNBC. El IVC **no** se guarda en casos: es un valor derivado de
varias tablas (ver calculos_indice).

**17. remisiones** — id_remision → (todo); {id_caso, id_abogado} → (todo). FNBC. `valor_cobrado`
podría parecer dependiente de una tarifa por tipo de daño, pero es el precio **pactado en el momento del
envío** (las tarifas cambian con el tiempo), así que es un hecho histórico de la remisión y no una DF.

**18. versiones_indice** — id_version → (todo); {nombre_indice, numero_version} → (todo). FNBC.
*Corrección hecha durante el diseño:* la propuesta inicial tenía una sola tabla
`parametros_indice(id, nombre_indice, variable, peso, version, vigente)`. Ahí `vigente` (y luego los
umbrales de nivel) dependían de {nombre_indice, version}, que es solo **parte** de la clave
{nombre_indice, version, variable}: una violación de 2FN que obligaba a repetir "vigente = true" en
cada variable, con riesgo de que unas filas de la misma versión dijeran true y otras false. Se separó en
versiones_indice (datos de la versión) y parametros_indice (peso de cada variable).

**19. parametros_indice** — {id_version, variable} → peso, descripcion. FNBC. La descripción explica cómo
**esa versión** normaliza la variable (los umbrales de normalización pueden cambiar entre versiones), por
eso depende de la clave completa y no solo de `variable`.

**20. calculos_indice** — id_calculo → id_reporte, id_version, valor, desglose, fecha_calculo. FNBC.
No se guarda el nombre del índice (se deriva de la versión) ni el nivel Bajo/Medio/Alto/Crítico (se
deriva del valor y de los umbrales de la versión); guardarlos sería introducir dependencias
transitivas (id_calculo → id_version → nombre_indice).

- **E4 — desglose JSONB.** Es la explicación del resultado ({variable: {valor, peso, aporte}}). Se guarda
  como un documento inmutable que se muestra completo: nunca se actualiza una parte ni se consulta por
  una variable concreta. Por eso se trata como valor atómico. Si en el futuro hubiera que consultar
  aportes por variable, se pasaría a una tabla `calculos_detalle(id_calculo, variable, valor, aporte)`.

## Conclusión

18 de 20 tablas están en FNBC sin observaciones. `reportes` está en FNBC, con una dependencia del mundo
real que no es verificable con los datos actuales (E1) y una redundancia controlada entre tablas (E2).
`incidentes` está en 3FN por una columna constante que existe para ganar integridad declarativa (E3).
Ninguna excepción permite anomalías de actualización sin control: cada una tiene un mecanismo de
verificación (CHECK, prueba o trigger planificado).
