# REDA — Documento del proyecto para revisión externa

> **Para quien revisa (persona o IA):** este documento describe el proyecto completo, porque tú no puedes verlo ni
> ejecutarlo. Al final (sección 12) están las preguntas concretas que nos interesan: **qué falta** y **cómo mejorar lo
> visual y la usabilidad**. Sé crítico y específico (qué cambiar, dónde y por qué); no hace falta que elogies lo que ya está.

---

## 1. Qué es REDA, en una frase

**REDA (Red de Evaluación y Detección de Riesgos Urbanos)** es una plataforma web para Valledupar (Cesar, Colombia) que
**convierte un reporte de infraestructura pública en mal estado en un caso legal documentado y viable**.

- Un ciudadano reporta una **falla** (hueco, alcantarilla sin tapa, luminaria dañada, señal caída…) con foto y ubicación.
  Eso queda como **antecedente**.
- Si alguien sufre un **incidente** (una lesión o un daño material por esa falla), REDA encuentra automáticamente los
  reportes previos del mismo punto: *"la entidad ya sabía desde hace 155 días"*. En Colombia eso sustenta una demanda de
  **reparación directa** contra el Estado por falla en el servicio.
- El ciudadano pide abogado; **varios abogados verificados se postulan** y **el ciudadano elige** con quién seguir.
  Al cerrarse el caso, **califica** al abogado (1 a 5 estrellas y un comentario).

**Modelo de negocio:** gratis para el ciudadano. Al abogado se le cobra una **cuota fija** cuando un ciudadano lo elige
(simulada: COP 200 000 si hubo lesión, COP 120 000 si fue solo daño material). No es cuota litis ni porcentaje de honorarios
(la regulación colombiana de la abogacía limita la intermediación; está pendiente de validar con un abogado real).

**Lo que REDA NO es:** no es asesoría jurídica, no garantiza resultados, no es una app genérica de quejas ciudadanas.

**Contexto académico:** proyecto de estudiantes de Ingeniería de Sistemas (Universidad Popular del Cesar) para dos
materias a la vez: **Base de Datos I** (PostgreSQL: modelo, migraciones, consultas, índices, normalización, después
funciones/triggers/seguridad/respaldos) y **Programación 3** (Java: POO, arquitectura en capas, validaciones y
excepciones, código limpio, patrones GRASP y principios SOLID, con auditorías). Habrá una **sustentación con demo en vivo**.

---

## 2. Usuarios y roles

| Rol | Quién es | Qué puede hacer |
|---|---|---|
| **Ciudadano** | Cualquier persona que se registra | Reportar fallas e incidentes, ver sus reportes, buscar abogado, comparar postulaciones, elegir, calificar |
| **Operador** | Personal de REDA | Revisar la bandeja de reportes y cambiar su estado (validar, rechazar con motivo…) |
| **Abogado** | Profesional independiente; se registra y queda **pendiente de verificación** | Ver casos disponibles (resumen anónimo), postularse, ver el expediente completo si lo eligen, cerrar el caso |
| **Administrador** | Personal de REDA | Lo del operador, más verificar abogados (activar su cuenta) |
| **Analista** | Futuro | Solo lectura (pendiente) |

---

## 3. Flujo principal de punta a punta (lo que se muestra en la demo)

1. Un ciudadano entra y **reporta un incidente**: marca el punto en un mini-mapa (o usa el GPS del celular), elige categoría
   y barrio, describe la falla y el daño, indica si hubo atención médica, agrega testigos y fotos.
2. Al enviar, REDA responde: **"La entidad ya sabía: 4 reportes previos; el primero, 155 días antes de lo que te pasó"**.
3. Un **operador** lo encuentra en su bandeja (los incidentes van primero) y lo valida.
4. El ciudadano pulsa **"Buscar abogado"** y autoriza explícitamente que los abogados vean un **resumen anónimo**.
5. Los **abogados** ven ese resumen (categoría, barrio, tipo de daño, si hubo atención médica, número de antecedentes,
   **valor que pagarían**) y **se postulan** con un mensaje.
6. El ciudadano **compara** a los postulados (experiencia, casos tomados, **reputación en estrellas y comentarios** de clientes
   anteriores, el mensaje de cada uno) y **elige**. En una sola transacción: postulación elegida, las demás cerradas,
   remisión facturable creada, caso "Aceptado por abogado".
7. Solo el abogado elegido ve el **expediente completo**: nombre y correo del ciudadano, descripción del daño, testigos,
   fotos, antecedentes con distancias, línea de tiempo. Al final **cierra** el caso.
8. El ciudadano **califica** al abogado.

---

## 4. Pantallas (descripción detallada)

**Estilo general:** todo en **modo oscuro** (fondo casi negro `#0d0d0d`, tarjetas `#1a1a19`, texto `#f0efec`), elementos
flotantes con efecto "vidrio esmerilado" (fondo translúcido + desenfoque), bordes redondeados (12–14 px), tipografía del
sistema (Segoe UI en Windows). Sin framework visual: HTML + CSS propio. Botón principal: claro sobre oscuro (casi blanco
con texto negro). Botón de peligro: rojo. Avisos flotantes ("toasts") abajo al centro. Diálogos propios (no `alert()`).

**Colores de "situación" de un caso** (validados para daltonismo sobre fondo oscuro, siempre acompañados de texto):
azul `#3987e5` = **Con abogado**, naranja `#d95926` = **Abierto**, verde agua `#199e70` = **Cerrado**.
Estrellas de calificación en ámbar `#eda100`. Mapa de calor en una sola gama de azules.

### 4.1 Pantalla de acceso (sin sesión) — `#/entrar`
Dos columnas en PC (una sola en celular). **Izquierda (portada):** logo (un anillo con los tres colores), titular grande
*"Cuando una falla de la ciudad te causa un daño, la evidencia es tu fuerza."* (las últimas palabras en azul claro), tres
viñetas (Reporta / Demuestra / Elige), fondo con degradados suaves, una cuadrícula diagonal tenue que sugiere calles y
círculos de colores que laten. **Derecha:** formulario de **Iniciar sesión** (correo, contraseña, botón "Entrar"), enlace
"¿Olvidaste tu contraseña?", y "¿No tienes cuenta? Crear cuenta · Soy abogado". Abajo: Términos · Privacidad · Ayuda.

Variantes en el mismo espacio de la derecha:
- **Crear cuenta** (ciudadano): nombre, correo, contraseña (regla: 8+ caracteres con letras y números), casilla
  obligatoria de aceptación de términos y tratamiento de datos (Ley 1581 de 2012).
- **Registro de abogados:** además tarjeta profesional, años de experiencia, especialidad, ciudad y presentación opcional;
  aviso de que la cuenta queda pendiente de verificación y de la cuota por caso.
- **Recuperar contraseña:** pide el correo; respuesta siempre igual (no revela si el correo existe). En este entorno no hay
  servidor de correo: el enlace aparece en la consola del servidor.
- **Nueva contraseña** (desde el enlace): dos campos y confirmación.

### 4.2 Barra superior (con sesión)
Flota sobre todo. Izquierda: logo "REDA · Valledupar". Al lado, el **menú según el rol** (pestañas con la activa resaltada):
- Ciudadano: Mapa · Reportar · Mis reportes · Mis casos
- Abogado: Mapa · Casos disponibles · Mis casos
- Operador: Mapa · Revisión · Administrador: Mapa · Revisión · Abogados

Solo en el mapa: tres cifras grandes (Reportes, Alta frecuencia, Incidentes). Derecha: avatar con iniciales y primer nombre;
al tocarlo, menú con nombre completo + rol, Mi cuenta, Ayuda, Privacidad, Cerrar sesión. En celular el menú de secciones
se colapsa en un botón "☰ Menú".

### 4.3 Mapa — `#/mapa`
Mapa a pantalla completa (Leaflet + teselas de OpenStreetMap oscurecidas con un filtro CSS) centrado en Valledupar.
- **Puntos agrupados:** los reportes a menos de 80 m se muestran como **un solo punto**. El tamaño crece con el número de
  reportes y el número va al centro. Alrededor, un **anillo segmentado** con la proporción de situaciones (azul, naranja,
  verde). Si en el punto hubo un incidente, el punto **late** (dos anillos blancos que se expanden). Debajo, un
  **mapa de calor** azul (se puede apagar).
- **Filtros** (barra bajo la barra superior): chips de situación con conteo ("Con abogado 2 · Abiertos 99 · Cerrados 30",
  se encienden/apagan), selector de categoría, interruptor "Solo alta frecuencia" (3+ reportes) y "Mapa de calor".
- **Leyenda** abajo a la izquierda ("Cómo leer el mapa"). Botón flotante "＋ Reportar" abajo a la derecha (solo ciudadanos).
  Aviso legal pequeño abajo al centro.
- **Al tocar un punto:** el mapa vuela hasta él (zoom 17) y se abre un **panel lateral derecho** (en celular, una hoja que
  sube desde abajo): barrio, "5 reportes desde el 19 de may de 2026", insignia de frecuencia (Única/Media/Alta/Crítica),
  barra apilada de situaciones con conteos, etiquetas de categorías, un filtro segmentado (Todos / Con abogado / Abiertos /
  Cerrados) y la **lista de reportes como línea de tiempo** (tarjetas con categoría, fecha, descripción en 3 líneas, chip de
  situación, estado de revisión, "Tu reporte" si es propio, "🔒 3 fotos").
- **Al tocar un reporte:** el panel cambia al detalle: tipo (⚠ Incidente / Condición), categoría, recuadro de situación con
  explicación, descripción, datos (zona, fecha, estado, gravedad percibida en ●●●○○), línea de tiempo de estados y
  **fotos**. Las fotos son **privadas**: solo las ven el autor, el personal de revisión y el abogado elegido; los demás ven
  un recuadro con candado "3 fotos privadas". El autor puede agregar fotos desde ahí. Las fotos se abren en un visor.
- Cada punto o reporte tiene **enlace directo** (`#/mapa/punto/1`, `#/mapa/reporte/137`).

### 4.4 Reportar — `#/reportar` (ciudadano)
Página con formulario en una tarjeta:
1. Dos tarjetas grandes para elegir: **"Una falla"** (nadie salió herido) o **"Un incidente"** (me causó un daño).
2. Categoría y barrio (dos listas desplegables).
3. **Mini-mapa** (300 px de alto) para tocar el punto exacto + botón "📍 Usar mi ubicación" (GPS) y el texto con las coordenadas.
   Al marcarlo, la lista de barrio (que está **arriba** del mapa) se llena sola: "Sugerido por el punto marcado (a 49 m).
   Cámbialo si no es tu barrio." Si eligió el barrio antes, el mapa se acerca a ese barrio.
4. Descripción con contador de caracteres. Gravedad percibida opcional: cinco botones 1–5.
5. Si es incidente, aparece un bloque con borde naranja "Lo que te pasó" (aviso de que es privado): fecha y hora del hecho,
   entidad que cree responsable (o "No sé"), tipo de daño (Lesión / Daño material / Ambos), casilla de atención médica
   (solo se habilita si hubo lesión), descripción del daño y testigos (hasta 5, se agregan con un botón).
6. Fotos: zona punteada "📷 Toca para tomar o elegir fotos", miniaturas con botón para quitar (máx. 5, JPG/PNG/WEBP ≤ 10 MB).
7. Botones Cancelar / Enviar reporte. Errores de validación junto a cada campo.

**Pantalla de resultado:** ✓ "Reporte #138 recibido", fotos guardadas, y si es incidente, un recuadro destacado
**"La entidad ya sabía · 4 reportes previos"** con la fecha del primero y los días de diferencia. Botones "Ver en el mapa" y
"Hacer otro reporte".

### 4.5 Mis reportes — `#/mis-reportes` (ciudadano)
Rejilla de tarjetas: categoría, fecha, barrio, número de reporte, fotos, descripción y una **barra de progreso por pasos**
(Pendiente → En revisión → Validado → En proceso → Solucionado). Si fue rechazado, nota roja. Botones: "Ver detalle y fotos",
**"Buscar abogado"** (solo incidentes validados sin caso; abre un diálogo que explica qué verán y qué no verán los abogados),
"Ver mi caso". Estado vacío con botón "Hacer mi primer reporte".

### 4.6 Mis casos — `#/mis-casos` (ciudadano)
Una tarjeta grande por caso: número, categoría · barrio, tipo de daño, chip de situación y estado.
- **Buscando abogado:** "3 abogados interesados — Compara y elige" y una rejilla de postulaciones. Cada una: avatar con
  iniciales, nombre, especialidad, **reputación** (★★★★☆ 4.0 · 1 calificación, o "Aún sin calificaciones"), años de
  experiencia, casos en REDA, presentación, desplegable "Lo que dicen sus clientes" (estrellas, comentario y
  "Cliente en Centro · mayo de 2026", sin nombre), el **mensaje** del abogado en una cita con borde azul, fecha y botón
  **"Elegir a Luis"** (confirmación que explica qué autoriza).
- **Con abogado:** recuadro azul "Tu abogado" con su perfil y correo de contacto.
- **Cerrado sin calificar:** formulario "¿Cómo te fue con tu abogado?" con 5 estrellas grandes seleccionables y comentario opcional.

### 4.7 Abogado — `#/abogado/disponibles` y `#/abogado/mis-casos`
- **Casos que buscan abogado:** tarjetas con resumen anónimo en una cuadrícula de datos (daño, atención médica, fecha del
  hecho, reportes previos destacados en azul con los días, testigos y fotos, entidad señalada), un bloque destacado
  **"Valor por tomar el caso: $ 120.000"** (se cobra solo si te eligen), número de postulados y botón **Postularme**
  (diálogo con el valor y un campo de mensaje de 20–1 500 caracteres) o "Te postulaste · Retirar".
- **Cuenta sin verificar:** pantalla "⏳ Tu cuenta está en verificación".
- **Mis casos:** total pagado a REDA, y por caso un **expediente** en dos columnas: ciudadano (nombre, correo), el hecho
  (fecha, daño, atención médica, entidad), descripción del daño y de la falla, testigos | antecedentes con distancia en metros,
  fotos, línea de tiempo. Botón **Cerrar caso** (diálogo con observación).

### 4.8 Operador — `#/revision`
"Bandeja de revisión — 36 reportes esperan revisión · 7 incidentes primero". Tarjetas (las de incidentes con borde naranja
a la izquierda): categoría, fecha y hora, barrio, estado, fotos, gravedad, descripción; botones "Ver fotos y ubicación" y
los **cambios de estado permitidos** (Tomar para revisión / Validar / Rechazar…). Rechazar exige motivo.

### 4.9 Administrador — `#/admin/abogados`
Tabla: abogado (nombre y correo), tarjeta profesional, especialidad y ciudad, experiencia, fecha de registro, estado
(Verificado / Pendiente) y botón **Verificar**.

### 4.10 Otras
**Mi cuenta** (nombre editable, correo fijo, datos del abogado y su reputación, cambio de contraseña con la actual, nota sobre
derechos de datos), **Ayuda** (preguntas frecuentes), **Términos** y **Privacidad** (borradores marcados para validar con abogado).

---

## 5. Arquitectura y tecnología

```
Navegador (HTML + CSS + JavaScript con módulos, sin framework; Leaflet 1.9.4 + leaflet.heat)
        │  HTTP/JSON, token JWT en la cabecera
API REST: Java 25 + Spring Boot 3.5 (Spring Web, Data JPA, Security, Validation, springdoc/Swagger)
        │  JPA para el CRUD + SQL explícito (JdbcTemplate) para mapa y búsqueda de antecedentes
PostgreSQL 18 (migraciones SQL numeradas 001–014, sin Flyway)
```
- La interfaz la sirve el mismo Spring Boot (`src/main/resources/static`): una URL, un comando. SPA con rutas `#/`,
  un módulo JS por vista, el menú cambia según el rol (el backend vuelve a verificar cada permiso).
- Capas del backend: `controller` (delgados) → `service` (un servicio por caso de uso) → `model` (entidades con
  comportamiento, sin setters) + `repository`; entrada y salida en `peticion` (formularios validados) y `respuesta` (JSON devuelto); transversales `exception` (jerarquía propia + manejador global único),
  `security` (JWT, BCrypt, límite de intentos), `config`.
- Patrones documentados con auditoría "hallazgo → corrección": Experto, Creador, Controlador, Alta cohesión, Bajo acoplamiento,
  Fabricación pura, Indirección, Variaciones protegidas; SRP, OCP, LSP, ISP, DIP.
- Pruebas: 81 unitarias (dominio sin base de datos, servicios con Mockito) y 17 de integración (HTTP → JPA → PostgreSQL real).

---

## 6. Modelo de datos (23 tablas, 3 vistas)

- **Catálogos:** roles, categorias (8), zonas (30 barrios), estados (6), estados_caso (6), entidades_publicas (7).
- **Personas:** usuarios (hash BCrypt, consentimiento), abogados (1:1 con usuario; verificado sí/no, experiencia, presentación).
- **Núcleo:** reportes (CONDICION/INCIDENTE, coordenadas, fecha), evidencias (fotos con hash SHA-256), evaluaciones,
  historial_estados (bitácora), reportes_relacionados (SIMILAR/ANTECEDENTE).
- **Legal:** incidentes (1:1 con reporte INCIDENTE: fecha del hecho, tipo de daño, atención médica), testigos, casos (1:1 con
  incidente), postulaciones, remisiones (lo que se factura), calificaciones (una por caso).
- **Índices de priorización (para la Entrega 3):** versiones_indice, parametros_indice (pesos versionados), calculos_indice.
- **Cuentas:** tokens_recuperacion (solo el hash del token).
- **Vistas:** v_mapa_publico (sin datos personales, con la situación del caso), v_resumen_casos, v_reputacion_abogados.
- Antecedentes: reportes de CONDICIÓN de la misma categoría, no rechazados, a ≤ 100 m (fórmula de Haversine, sin PostGIS),
  anteriores a la fecha del hecho.

---

## 7. Privacidad y seguridad (ya implementado)

- Todo exige sesión salvo entrar, crear cuenta, recuperar contraseña y páginas legales.
- Fotos privadas (cabecera `no-store`); formato validado por los bytes del archivo, máx. 10 MB, sin duplicados (hash),
  nombre de archivo generado.
- Datos de salud (descripción del daño, atención médica) y testigos: solo operador/administrador y el abogado **elegido**.
- Abogados no elegidos ven un resumen anónimo. Comentarios de calificación sin nombre del autor.
- JWT con expiración, BCrypt, 8 intentos/15 min en login y recuperación (429), CORS restrictivo, secretos por variables de
  entorno, mensajes de error uniformes que no filtran detalles internos.

---

## 8. Datos

Todos los datos de la demo son **simulados** (137 reportes, 18 incidentes, 10 casos, 55 usuarios ficticios con correos
`.test`). Los nombres de barrios y entidades son de Valledupar.
**Ubicaciones:** cada uno de los 30 barrios tiene un punto de referencia **real** tomado de OpenStreetMap y comprobado
contra el límite de su comuna; los reportes simulados caen a menos de 170 m de ese punto. Al marcar el punto en el
formulario, la app **sugiere el barrio** (si alguno queda a 600 m o menos) y el ciudadano puede cambiarlo. Límite: solo 30
de los ~175 barrios están ubicados (OSM casi no tiene barrios de Valledupar dibujados y la Alcaldía no publica polígonos).

---

## 9. Lo que funciona hoy (resumen)

Registro/login/recuperación · reportar con mapa, GPS y fotos · antecedentes automáticos · mapa agrupado con calor, filtros y
panel · fotos privadas · bandeja del operador · buscar abogado con consentimiento · postulaciones · elección del ciudadano ·
expediente para el abogado elegido · cierre · calificaciones y reputación · verificación de abogados · mi cuenta · páginas
legales · diseño adaptable a celular (probado a 390 px) · instalable como app (manifest web, sin modo sin conexión).

---

## 10. Lo que sabemos que falta o es débil

- **Notificaciones:** nadie se entera de nada si no entra a mirar (el ciudadano no sabe que un abogado se postuló; el
  abogado no sabe que lo eligieron). No hay correo real ni notificaciones push.
- **Mensajería** ciudadano–abogado dentro de la plataforma (hoy solo se da el correo).
- **Dashboard** para el administrador (cifras, casos más viables, tiempos de atención) e **índices IRU/IVC** (riesgo urbano
  y viabilidad del caso, con desglose), planeados para la Entrega 3.
- El operador no puede editar un reporte (corregir categoría o barrio) ni marcar duplicados desde la interfaz.
- No hay buscador ni paginación (con miles de reportes sería un problema).
- Sin modo claro; sin traducción; sin modo sin conexión.
- Fotos de la semilla son imágenes de ejemplo; no hay compresión ni lectura de metadatos (fecha/GPS de la foto).
- Recuperación de contraseña por consola (sin correo real); sin verificación del correo al registrarse.
- Moderación de comentarios de calificación (hoy se publican tal cual).
- Plazos legales (caducidad de la acción) no se muestran ni se calculan; textos legales en borrador.

---

## 11. Restricciones que la revisión debe respetar

- Tecnologías fijas: **Java/Spring Boot + PostgreSQL**; la interfaz es HTML/CSS/JS sin framework (se puede proponer uno si se justifica).
- Tiempo y equipo de estudiantes: priorizar mejoras de alto impacto y bajo costo.
- La privacidad no se negocia: no proponer mostrar datos personales, fotos o datos de salud de forma pública.
- No presentar REDA como asesoría jurídica ni prometer resultados.
- Debe funcionar en la demo en vivo y en celulares de gama media en Colombia.

---

## 12. Lo que te pedimos

1. **Qué le falta** a la plataforma para que un ciudadano, un abogado y un operador la usen de verdad (funciones, flujos,
   casos límite, confianza).
2. **Usabilidad:** ¿dónde se perdería o se frustraría un usuario? (registro, reportar desde el celular en la calle, entender
   el mapa, comparar abogados, entender qué pasa con su caso). ¿Qué textos, pasos o estados vacíos cambiarías?
3. **Visual:** jerarquía, legibilidad del modo oscuro, densidad de información, consistencia, el mapa (¿se entienden el
   anillo, el pulso y el calor?), las tarjetas de postulación, el formulario de reporte.
4. **Accesibilidad:** contraste, tamaños táctiles, lectores de pantalla, personas mayores o con poca experiencia digital.
5. **Confianza y seguridad percibida:** ¿qué haría que un ciudadano confíe en dar datos de salud o que un abogado pague?
6. **Para la sustentación:** ¿qué mejora pequeña haría la demo más impactante?

Responde con una lista **priorizada** (impacto alto/medio/bajo y esfuerzo), indicando la pantalla afectada.
