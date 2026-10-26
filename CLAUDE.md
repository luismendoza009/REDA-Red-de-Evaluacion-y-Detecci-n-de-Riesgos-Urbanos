# CLAUDE.md — Proyecto REDA (Base de Datos I + Programación 3)

> Este archivo es tu memoria del proyecto. Léelo completo antes de hacer cualquier cosa. Si algo de aquí contradice lo que el usuario te diga después, manda lo que el usuario diga, pero avísale de la contradicción.

---

## 0. CÓMO TRABAJAR CON EL USUARIO

- El usuario se llama Luis, estudiante de Ingeniería de Sistemas en la Universidad Popular del Cesar (Unicesar), Valledupar. Trabaja en Windows (Lenovo), con VS Code, PowerShell y Git.
- Idioma: español, siempre. Lenguaje claro y directo, sin relleno.
- Entrega el código completo y funcional, listo para copiar y pegar. Nada de pseudocódigo ni "completa tú aquí".
- Explica brevemente cada decisión técnica importante (una o dos líneas, no ensayos).
- Trabaja sin pedir confirmación en cada paso. Solo pregunta cuando falte información que no puedes deducir (ver sección 14).
- Sé honesto: si algo del plan es mala idea, dilo y propón alternativa. No halagues ni rellenes.
- Nunca inventes referencias académicas, autores, DOI ni normas. Si necesitas citar algo, di "pendiente de verificar" o búscalo.
- Los comandos de terminal deben funcionar en **PowerShell de Windows** (no bash). Ejemplo: `type nul > archivo`, `copy`, `move`, `dir`.

---

## 1. QUÉ ES REDA

**REDA — Red de Evaluación y Detección de Riesgos Urbanos.** Plataforma web para Valledupar (Cesar, Colombia), diseñada para poder escalar a otras ciudades.

### 1.1 Enfoque ACTUAL (cambió respecto a la idea inicial)

La idea original era vender priorización de reportes a alcaldías. **Eso ya NO es el enfoque.** El cliente ahora es el **ciudadano**, y el negocio es este:

> Cuando la infraestructura pública en mal estado (huecos, alcantarillas, postes, señalización, etc.) causa un accidente o daño a un ciudadano, el Estado puede ser responsable (en Colombia, la figura es la acción de reparación directa por falla en el servicio). REDA ayuda al ciudadano a **documentar el hecho con evidencia sólida** y, si el caso tiene mérito, lo **conecta con un abogado** para reclamar reparación por daños y perjuicios.

**Propuesta de valor en una frase:** *REDA convierte un reporte de infraestructura en mal estado en un caso legal documentado y viable.*

### 1.2 Modelo de negocio

- Lead generation / intermediación tecnológica. No se cobra al ciudadano. Se cobra al abogado o firma por **cada caso remitido** (cuota fija por lead calificado).
- **NO** cuota litis ni participación en honorarios: en Colombia el ejercicio de la abogacía está regulado y la captación de clientes / pactos con intermediarios no abogados tiene restricciones. Esto debe aparecer en la documentación como riesgo pendiente de validar con un abogado real.
- El usuario fue claro: al negocio no le interesa "prevenir", le interesa la **recolección de evidencia**. No vendas ni documentes el proyecto como un sistema de prevención de riesgos; preséntalo honestamente como **sistema de documentación probatoria de fallas en infraestructura pública**.

### 1.3 Decisión de producto: dos tipos de reporte, una sola base de datos

```
Tipo 1: REPORTE DE CONDICIÓN (antecedente)
  Foto + ubicación + fecha + categoría. Nadie salió lastimado todavía.
  Sirve como antecedente probatorio: demuestra que la entidad "ya sabía".

Tipo 2: REPORTE DE INCIDENTE (núcleo del negocio)
  Todo lo anterior + daño sufrido (lesión/daño material), fecha y hora
  exactas, atención médica, testigos. Se vincula automáticamente a los
  reportes de condición previos en un radio cercano.
```

El vínculo entre un incidente y sus antecedentes es la función estrella del sistema: "este punto tiene 4 reportes previos desde hace 5 meses" hace el caso más fuerte y el lead más valioso.

---

## 2. ESTADO ACTUAL DEL REPOSITORIO

- Repo: https://github.com/luismendoza009/REDA-Red-de-Evaluacion-y-Detecci-n-de-Riesgos-Urbanos
- Integrantes: Rafael Alfonso Rivera Restrepo, Ivan Andres Rodriguez Rodriguez, Luis Eliecer Mendoza Perpiñán.
- **Entrega 1 (diseño):** documento Word/PDF hecho (`documentacion/Entrega1_REDA.docx` y `.pdf`). Esquema de 10 tablas: `roles, usuarios, categorias, zonas, estados, reportes, evidencias, evaluaciones, historial_estados, reportes_relacionados`.
- **PENDIENTE de Entrega 1:** la carpeta `diagramas/` está VACÍA. Los archivos `reda_eer.drawio` y `reda_eer.png` deben copiarse ahí. Después: commit, push, y mover el tag `entrega-1` al commit final (`git tag -d entrega-1; git push origin :refs/tags/entrega-1; git tag -a entrega-1 -m "..."; git push origin entrega-1`).
- Si los archivos del diagrama no existen en el equipo, regenera el diagrama (ver sección 5) como `.drawio` y también como imagen `.png`.
- Problema de autenticación Git que ocurrió: el equipo estuvo autenticado con otra cuenta de GitHub (`leliecermendoza`) y dio error 403 contra la cuenta dueña (`luismendoza009`). Si vuelve a pasar, guía al usuario a limpiar la credencial en el Administrador de credenciales de Windows.

---

## 3. UN PROYECTO, DOS ASIGNATURAS

El mismo sistema se entrega en dos materias. Diseña todo para que sirva a ambas sin duplicar trabajo.

```
BASE DE DATOS I        → diseño, PostgreSQL, SQL, normalización, índices,
                          funciones/triggers/transacciones, seguridad, respaldos
PROGRAMACIÓN 3         → aplicación Java (backend) + interfaz (frontend),
                          arquitectura por capas, buenas prácticas
```

**Programación 3 NO tiene guía escrita** (confirmado por Luis). Temas vistos en clase, que el código debe evidenciar y la documentación debe señalar: Oh My Posh (terminal), POO, arquitectura en capas, integración, validaciones y excepciones, código limpio v2, patrones GRASP, auditoría y corrección GRASP, auditoría y corrección SOLID. Por eso: `documentacion/programacion3/` debe tener un mapa "tema → dónde está en el código" y las auditorías GRASP y SOLID con formato hallazgo → corrección. Usa el stack de la sección 8.

---

## 4. REQUISITOS DE BASE DE DATOS I (guía del profesor — OBLIGATORIOS)

Reglas generales: grupo ≥ 4 integrantes (el repo lista 3; confirmar si hay un cuarto), tema propio con 6–8+ tablas y reglas de negocio reales, PostgreSQL 16+, Git desde la Entrega 1, diagramas en Draw.io. **Lo que no está en el repositorio no existe.** Cada entrega = repositorio con tag + PDF breve que indica dónde encontrar todo. Todos los integrantes deben hacer commits y poder explicar cualquier parte (la sustentación incluye preguntas individuales).

### Entrega 1 — Planteamiento y diseño (10 % del corte 1) — CASI LISTA
Planteamiento, requisitos + preguntas/supuestos, EER completo, esquema relacional con políticas de borrado, diccionario de datos, repo con README. Tag: `entrega-1`.

### Entrega 2 — Implementación (10 % del corte 2)
```
1. Correcciones de la Entrega 1 incorporadas, con nota de qué cambió y por qué
2. DDL como migraciones numeradas: migraciones/001_xxx.sql, 002_xxx.sql...
   ejecutables EN ORDEN sobre una base VACÍA. Una migración aplicada no se
   edita: se corrige con la siguiente.
   Integridad declarativa: PK, FK con política, NOT NULL, UNIQUE, CHECK
3. Datos semilla coherentes (nada de cliente1, cliente2). Sin datos reales.
4. Auditoría de normalización: dependencias funcionales listadas + verificación
   3FN/BCNF por tabla (o excepción justificada)
5. Banco de consultas: MÍNIMO 8 consultas de negocio, comentadas con la
   pregunta que responden. Deben incluir como mínimo:
     - 2 con reunión (JOIN) de 3 o más tablas
     - 1 reunión externa con patrón "los que no tienen"
     - 1 agregación con GROUP BY y HAVING
     - 1 subconsulta o CTE
     - 1 vista creada y consultada
6. Índices con evidencia: consulta + EXPLAIN ANALYZE antes + índice + EXPLAIN
   ANALYZE después + conclusión. FK de joins frecuentes indexadas.
7. Diccionario de datos actualizado al esquema real
```
Tag: `entrega-2`. Errores que más cuestan: migraciones que fallan sobre base vacía (PRUÉBALAS), tablas sin FK, consultas sin verificar a mano, totales inflados por unir antes de agregar, CHECK que faltan, índices sin plan antes/después.

### Entrega 3 — Producto final y sustentación (20 % del corte 3)
```
1. Todo lo anterior, corregido (acumulativo)
2. Funcionalidad avanzada:
     - 2 funciones (al menos 1 en PL/pgSQL) con lógica real del dominio
     - 2 triggers justificados (al menos 1 de auditoría o validación que las
       restricciones declarativas no alcancen). Nada de triggers decorativos.
     - 1 operación transaccional demostrada (todo-o-nada) + explicar qué
       pasaría sin la transacción
3. Seguridad: ≥ 2 roles con privilegios mínimos documentados (ej. rol de la
   aplicación y rol de análisis), una vista que materialice el acceso
   restringido del rol de análisis, columnas con datos personales
   identificadas en el diccionario con su medida de protección
4. Operación: plan de respaldos (comando/script, frecuencia, dónde se guardan)
   + EVIDENCIA de restauración probada sobre una base vacía
5. Documentación final: planteamiento, diagrama final, esquema, diccionario
   completo, decisiones y compromisos, DECLARACIÓN DE USO DE IA
6. Sustentación oral con demo EN VIVO (no pantallazos), todos exponen
```
Tag: `entrega-final`. Credenciales JAMÁS en el código ni en el repo.

### Política de IA del curso
Se permite usar IA, pero es obligatorio declarar para qué se usó, en qué partes y cómo se verificó. El grupo debe poder explicar cada línea. **Tu trabajo incluye ayudar a que el usuario entienda lo que entregas**: deja comentarios claros y explica las decisiones para que pueda defenderlas oralmente. Genera además un borrador de `documentacion/declaracion_uso_ia.md`.

---

## 5. MODELO DE DATOS OBJETIVO

### 5.1 Base (Entrega 1, ya diseñada)

```
roles(id_rol PK, nombre UNIQUE)
usuarios(id_usuario PK, nombre, correo UNIQUE, password_hash, id_rol FK, fecha_registro)
categorias(id_categoria PK, nombre UNIQUE, descripcion)
zonas(id_zona PK, nombre, tipo)
estados(id_estado PK, nombre UNIQUE)
reportes(id_reporte PK, id_usuario FK, id_categoria FK, id_zona FK, id_estado FK,
         descripcion, latitud, longitud, fecha, nivel_gravedad CHECK 1..5)
evidencias(id_evidencia PK, id_reporte FK CASCADE, ruta_imagen, fecha)
evaluaciones(id_evaluacion PK, id_reporte FK CASCADE, gravedad, impacto, frecuencia, fecha_evaluacion)
historial_estados(id_historial PK, id_reporte FK CASCADE, id_estado_anterior FK,
                  id_estado_nuevo FK, id_usuario FK, fecha, observacion)
reportes_relacionados(id_reporte FK, id_reporte_relacionado FK, PK compuesta, CHECK distintos)
```
Políticas: RESTRICT en FK de catálogos/usuarios (un reporte es evidencia histórica); CASCADE en hijos que no existen sin el reporte.

### 5.2 Extensión por el nuevo enfoque (a incorporar en Entrega 2, DOCUMENTANDO el cambio respecto a Entrega 1)

Propuesta inicial; ajústala si al normalizar encuentras algo mejor, pero justifícalo:

```
reportes: agregar tipo_reporte ('CONDICION' | 'INCIDENTE') con CHECK

incidentes(id_incidente PK, id_reporte FK UNIQUE → reportes (1:1 con reportes
           de tipo INCIDENTE), fecha_hora_hecho, descripcion_dano,
           tipo_dano ('LESION'|'DANO_MATERIAL'|'AMBOS'),
           hubo_atencion_medica BOOLEAN, entidad_presuntamente_responsable)
testigos(id_testigo PK, id_incidente FK CASCADE, nombre, contacto)
entidades_publicas(id_entidad PK, nombre UNIQUE, tipo, ciudad)
   -- a quién se le atribuye la falla (alcaldía, secretaría, empresa de servicios)
abogados(id_abogado PK, id_usuario FK UNIQUE, tarjeta_profesional UNIQUE,
         especialidad, ciudad, activo)
casos(id_caso PK, id_incidente FK UNIQUE, id_estado_caso FK, indice_viabilidad,
      fecha_creacion)
remisiones(id_remision PK, id_caso FK, id_abogado FK, fecha_envio, estado_remision,
           valor_cobrado)   -- el "lead" que se le cobra al abogado
estados_caso(id_estado_caso PK, nombre)
   -- Borrador, Evaluado, Remitido, Aceptado por abogado, Rechazado, Cerrado
parametros_indice(id_parametro PK, nombre_indice, variable, peso, version, vigente)
   -- pesos configurables y versionados de los índices (ver sección 6)
auditoria(id_auditoria PK, tabla, id_registro, accion, usuario, fecha, datos_anteriores JSONB, datos_nuevos JSONB)
```
Roles: `CIUDADANO`, `OPERADOR`, `ABOGADO`, `ADMINISTRADOR`, (futuro: `ANALISTA`).

Vínculo incidente ↔ antecedentes: función SQL que, dado un incidente, devuelve los reportes de condición de la misma categoría dentro de un radio (ej. 50–100 m, configurable) y con fecha anterior al hecho. Usa fórmula de Haversine en PL/pgSQL o SQL (no hace falta PostGIS; si lo usas, justifícalo y que las migraciones sigan siendo reproducibles).

### 5.3 Diagrama
Genera/actualiza `diagramas/reda_eer.drawio` (XML válido de draw.io, notación pata de gallo, 13+ relaciones, anclajes separados para que las líneas no se encimen) y exporta `diagramas/reda_eer.png` si tienes herramienta para ello; si no, dile al usuario cómo exportarlo desde app.diagrams.net. Layout de referencia: catálogos arriba, `reportes` al centro, tablas dependientes abajo.

---

## 6. ÍNDICES DE PRIORIZACIÓN (la parte "inteligente", defendible académicamente)

**Regla de oro: NO mezcles dos conceptos distintos en un solo número.** Mezclarlos sería un conflicto de interés (priorizar lo más rentable de litigar y llamarlo "riesgo objetivo"). Hay dos índices separados, ambos 0–100:

```
IRU — Índice de Riesgo Urbano (severidad de la condición, para el ciudadano/ciudad)
  variables candidatas: gravedad percibida, número de reportes similares cercanos,
  antigüedad sin atención, tráfico de la zona, cercanía a colegios/hospitales,
  historial de accidentes en la zona

IVC — Índice de Viabilidad del Caso (para el abogado/negocio)
  variables candidatas: daño documentado, calidad de la evidencia (fotos, ubicación,
  hora), nº de antecedentes previos en el punto, antigüedad del primer antecedente
  (la entidad "ya sabía"), existencia de atención médica registrada, testigos,
  tiempo transcurrido desde el hecho (existe un plazo de caducidad para demandar:
  VERIFICAR con un abogado; mi entendimiento es 2 años, no lo des por hecho)
```

Metodología propuesta (multicriterio simple y defendible):
1. Normalizar cada variable a 0–100 (min-max, o por reglas/umbrales cuando la variable es categórica).
2. Combinar con **suma ponderada**: `Índice = Σ (peso_i × valor_i)`, con Σ pesos = 1.
3. Pesos iniciales por criterio experto, guardados en `parametros_indice` (versionados, configurables). Para el proyecto de grado: derivarlos con un método formal (por ejemplo AHP) a partir de encuestas a abogados y funcionarios. **No inventes citas**; si hace falta bibliografía, déjala marcada "por verificar" o búscala.
4. Clasificar en niveles. Los rangos 0–24 / 25–49 / 50–74 / 75–100 (Bajo/Medio/Alto/Crítico) son **preliminares**; deben calibrarse con datos y criterio experto, y dejarse configurables.
5. Cada resultado debe poder **explicarse** ("estas son las razones"): guarda el desglose por variable, no solo el total.

Variables difíciles de conseguir (tráfico, cercanía a colegios, historial de accidentes): para el proyecto de aula van como **datos simulados documentados** asociados a la zona (ver sección 11). Marca en el diccionario y en la documentación cuáles columnas son simuladas.

Esto alimenta la Entrega 3 (funciones PL/pgSQL que calculan IRU e IVC) y Programación 3 (visualización y priorización).

---

## 7. LO QUE EL PROYECTO NO DEBE SER

- No un "CRUD con interfaz bonita". Debe verse la cadena: **datos → evidencia → vínculo con antecedentes → índice → decisión (remitir o no) → seguimiento**.
- No una app genérica de reportes ciudadanos (eso ya existe).
- No dar asesoría jurídica: la plataforma **no es un abogado** y no afirma que un caso ganará. Incluye avisos claros en la interfaz y términos de uso.
- Nada de ML en esta etapa. Se deja como trabajo futuro (proyecto de grado) y solo si hay datos reales suficientes.

---

## 8. ARQUITECTURA Y STACK (justifica cada tecnología en la documentación)

```
Frontend (SPA)  ──HTTP/JSON──▶  API REST (Spring Boot, Java 17+)  ──JDBC──▶  PostgreSQL 16+
```

- **Backend:** Java 17 o 21, Maven, Spring Boot 3.x, Spring Web, Spring Data JPA (o JdbcTemplate donde las consultas SQL del curso deban verse explícitas), Spring Security + JWT, Bean Validation, springdoc-openapi (Swagger) para documentar endpoints.
- **Contraseñas:** BCrypt. Nunca texto plano.
- **Capas:** `controller / service / repository / model(entity) / dto / exception / config / security`. Los controllers no tocan repositorios directamente; los DTO separan la API de las entidades; manejo global de errores con `@RestControllerAdvice`.
- **SQL:** consultas parametrizadas siempre (prevención de SQL Injection). Las consultas del banco de la Entrega 2 viven como SQL puro en `sql/consultas/` y, cuando aplique, también expuestas por endpoints para el dashboard.
- **Migraciones:** scripts numerados `migraciones/001_...sql` (formato exigido por el profesor). Crea un script `scripts/migrar.ps1` que cree la base vacía y ejecute las migraciones en orden con `psql`, para poder reconstruir todo desde cero en la demo. No uses Flyway con su convención de nombres salvo que puedas respetar el formato `001_...` del profesor.
- **Frontend:** por defecto **Angular** (standalone components, TypeScript) + **Leaflet + OpenStreetMap** para el mapa (gratis, sin API key). Si Programación 3 exige o permite otra cosa más simple (p. ej. JavaFX/Swing, o HTML+JS), adáptate a eso — consúltalo primero (sección 14). No añadas tecnologías sin justificación.
- **Archivos (fotos):** para el prototipo, almacenamiento en disco del servidor con ruta guardada en `evidencias.ruta_imagen`; valida tipo y tamaño; guarda nombre generado (no el original); lee metadatos mínimos. Documenta que en producción iría a almacenamiento de objetos.
- **Configuración:** variables de entorno / `application-local.properties` (en `.gitignore`). Entrega un `application-example.properties` sin secretos.

---

## 9. FUNCIONALIDAD QUE DEBE TENER EL PRODUCTO

**Ciudadano:** registro e inicio de sesión; crear reporte de condición o de incidente (con foto, ubicación en mapa con GPS, descripción, categoría, y datos de daño si es incidente); ver el estado y la línea de tiempo de sus reportes; ver sus casos y a qué abogado fueron remitidos; ver mapa público de reportes.
**Operador:** revisar/validar reportes, cambiar estados con observación (queda en historial), completar evaluaciones, revisar el vínculo con antecedentes, marcar un incidente como caso viable.
**Abogado:** ver casos remitidos con el expediente de evidencia (fotos, línea de tiempo, antecedentes previos, índices con desglose), aceptar o rechazar.
**Administrador:** gestionar usuarios, categorías, zonas, entidades, abogados, parámetros/pesos de índices; dashboard.

**Dashboard (visual, convincente):** total de reportes; condiciones vs. incidentes; casos por estado; distribución por nivel (Bajo/Medio/Alto/Crítico) para IRU; **Top 10 casos más viables** (IVC) con sus razones; reportes por categoría y por zona; tiempo promedio de atención/remisión; evolución temporal; **mapa** con marcadores por tipo y nivel, y agrupación de puntos con antecedentes.

---

## 10. SEGURIDAD Y PRIVACIDAD

- JWT con expiración, autorización por rol en cada endpoint (`@PreAuthorize`), validación de entrada, CORS restrictivo, sin secretos en el repo.
- Roles de base de datos separados (Entrega 3): `reda_app` (CRUD mínimo necesario) y `reda_analista` (solo lectura mediante vistas que ocultan datos personales).
- Datos personales y **sensibles** (los datos de lesiones/atención médica son datos de salud): identifícalos en el diccionario con la medida de protección. Colombia tiene régimen de protección de datos personales (Ley 1581 de 2012, "Habeas Data"); redacta una política de privacidad y un consentimiento informado en el registro, marcando que debe validarse con un abogado. No afirmes cumplimiento legal que no puedes garantizar.
- Auditoría: tabla `auditoria` alimentada por triggers sobre `reportes`, `casos` y `remisiones` (uno de los 2 triggers obligatorios de la Entrega 3).
- Los reportes de otros usuarios no deben exponer datos personales del autor en el mapa público.

---

## 11. DATOS

- Semilla simulada pero **coherente y realista para Valledupar**: barrios/zonas reales de la ciudad (nombres de barrios sí; ningún dato personal real), coordenadas plausibles dentro del casco urbano, fechas distribuidas en varios meses, categorías (hueco, alcantarilla obstruida, luminaria dañada, señalización deteriorada, residuos, árbol en riesgo, fuga de agua, otros), entidades responsables plausibles, 100–150 reportes con variedad, algunos incidentes con antecedentes previos cercanos para que la demo luzca.
- Marca todo dato simulado como tal (columna o documento `documentacion/datos_simulados.md`) y documenta cómo se pasaría a datos reales: qué se obtiene públicamente (cartografía, división barrial, datos abiertos de la ciudad), qué por encuestas (pesos de los índices con expertos), qué sigue simulado y cómo se evita que lo simulado invalide conclusiones (separar claramente en cualquier experimento).
- Verifica fuentes públicas reales buscándolas; no asumas que un conjunto de datos existe.

---

## 12. ESTRUCTURA DEL REPOSITORIO

```
/
├─ README.md                      (identificación, integrantes, cómo ejecutar)
├─ CLAUDE.md                      (este archivo)
├─ documentacion/
│   ├─ Entrega1_REDA.docx / .pdf
│   ├─ entrega2/  (PDF + notas de correcciones + auditoría de normalización + banco de consultas + evidencia de índices)
│   ├─ entrega3/  (documento integrador + declaración de IA + plan de respaldos + evidencia de restauración)
│   ├─ programacion3/  (guía de la materia y documentos de esa entrega)
│   ├─ diccionario_datos.md
│   └─ decisiones.md  (decisiones técnicas y compromisos, con su porqué)
├─ diagramas/                     (reda_eer.drawio + reda_eer.png, arquitectura)
├─ migraciones/                   (001_...sql, 002_...sql ...)
├─ sql/
│   ├─ semilla/        (datos semilla)
│   ├─ consultas/      (banco de consultas comentadas)
│   ├─ funciones/ y triggers/
│   └─ seguridad/      (roles, grants, vistas)
├─ scripts/            (migrar.ps1, respaldo.ps1, restaurar.ps1)
├─ backend/            (Spring Boot, Maven)
└─ frontend/           (Angular u otra tecnología acordada)
```

---

## 13. FLUJO DE GIT (el usuario quiere ramas y commits limpios)

- Rama `main` siempre estable. Trabajo en ramas: `feature/<tema>`, `fix/<tema>`, `docs/<tema>`. Integración por Pull Request.
- Commits pequeños e independientes, en español, formato tipo: `feat: agrega migración 003 de incidentes`, `fix: corrige CHECK de nivel_gravedad`, `docs: actualiza diccionario de datos`, `test: ...`.
- **Todos los integrantes deben tener commits reales** (la rúbrica lo penaliza). Cuando sea sensato, reparte tareas por persona y dilo: por ejemplo, un integrante migraciones, otro consultas, otro backend, otro frontend. No hagas commits a nombre de otros ni falsifiques autoría; recuérdale al usuario que cada compañero debe usar su propio usuario de Git.
- Tags de entrega: `entrega-1`, `entrega-2`, `entrega-final`.
- Nunca subas credenciales, `.env`, archivos de configuración local ni datos personales reales. Revisa el `.gitignore` antes del primer commit del backend.
- Antes de cada tag, **verifica que el proyecto reconstruye desde cero**: base vacía → migraciones → semilla → backend arranca → demo funciona.

---

## 14. PRIMEROS PASOS (haz esto en orden)

1. Lee este archivo, `README.md` y revisa `git status` y `git log --oneline`.
2. **Pregunta al usuario SOLO esto (en un único mensaje) y espera respuesta:**
   - ¿Tiene la guía/enunciado de **Programación 3** (fechas, tecnologías obligatorias, rúbrica)? Si sí, que la copie a `documentacion/programacion3/`.
   - ¿Fechas de Entrega 2 y Entrega 3 de Base de Datos?
   - ¿Quién es el cuarto integrante (la guía exige ≥ 4) y sus usuarios de GitHub?
   - ¿Qué versiones tiene instaladas (Java, Maven, PostgreSQL, Node)? Verifícalo tú con comandos (`java -version`, `mvn -v`, `psql --version`, `node -v`) antes de preguntar.
3. Cierra Entrega 1: coloca el diagrama en `diagramas/`, commit, push, mueve el tag `entrega-1`.
4. Crea la rama `feature/entrega-2-migraciones` y construye, en este orden, probando cada paso:
   migraciones DDL → `scripts/migrar.ps1` → semilla → auditoría de normalización → banco de ≥ 8 consultas → índices con EXPLAIN ANALYZE → diccionario actualizado → documento PDF de Entrega 2. Tag `entrega-2`.
5. Backend Spring Boot (puede avanzar en paralelo a partir de las migraciones): autenticación + roles → CRUD de catálogos → reportes (condición e incidente) con fotos → historial de estados → vínculo con antecedentes → casos/remisiones → endpoints de dashboard. Documenta con Swagger.
6. Frontend: login → formulario de reporte con mapa → mis reportes → panel de operador → vista de abogado → dashboard con mapa.
7. Entrega 3 de BD: funciones PL/pgSQL (cálculo de IRU/IVC y búsqueda de antecedentes por radio), 2 triggers (auditoría + validación), transacción (p. ej. "crear caso + remisión + historial" todo-o-nada), roles y vista restringida, respaldo y **restauración probada**, documento integrador, declaración de uso de IA. Tag `entrega-final`.
8. Prepara la demo (sección 15) y ensáyala desde una base reconstruida con las migraciones.

---

## 15. DEMO (objetivo principal de presentación)

Guion de 5–7 minutos, que muestre una **transformación**, no una lista de tecnologías:

```
1. Problema (30 s): una persona se cae en una alcantarilla abierta. ¿Y ahora qué?
2. Un ciudadano reporta el incidente desde el celular: foto, ubicación, daño sufrido.
3. REDA encuentra automáticamente 4 reportes previos en ese punto, el primero de
   hace 5 meses: la entidad "ya sabía".
4. Se calculan IRU e IVC, con el desglose de razones.
5. El operador valida y el caso se remite a un abogado con el expediente completo.
6. El abogado ve el caso; el dashboard muestra los Top 10 casos más viables.
7. Cierre: de "500 reportes dispersos" a "estos son los casos que importan, y por qué".
```
Lleva siempre: un reset reproducible de la base (script), usuarios de prueba por rol, y un plan B (capturas/video) por si falla la red.

---

## 16. REGLAS DE CALIDAD

- Pruébalo todo antes de decir que funciona: ejecuta las migraciones sobre base vacía, corre las consultas y compara contra un cálculo a mano, ejecuta los tests del backend. No afirmes resultados que no verificaste.
- Verifica que las consultas no inflen totales (agrega antes de unir cuando haga falta).
- Cada trigger y función con su justificación escrita en `documentacion/decisiones.md`.
- Pruebas: unitarias de servicios (incluido el cálculo de índices con casos de borde) y de integración de endpoints críticos.
- Todo en español en la documentación; nombres de código consistentes (tablas y columnas en español en snake_case, como en el diccionario; clases Java en inglés o español, pero consistentes y acordado con el usuario).
- Documentos académicos: APA 7, Times New Roman 12 pt. Para entregas en PDF/Word, plantilla institucional de Unicesar si el usuario la proporciona.
- Si dudas entre hacer más cosas o hacer mejor las que la rúbrica pide, **prioriza la rúbrica**. Lo extra solo entra si lo obligatorio ya está sólido.

---

## 17. RIESGOS CONOCIDOS (inclúyelos en la documentación, no los ocultes)

- **Legal:** regulación de la abogacía y captación de clientes; valor probatorio real de fotos/geolocalización desde una app; plazos de caducidad; tratamiento de datos de salud. Requiere validación con un abogado real.
- **Académico:** pesos de índices sin validación experta (por eso son configurables y se validarán en el proyecto de grado); datos simulados (documentar y separar).
- **Técnico:** manipulación de reportes falsos (mitigar con validación del operador, metadatos de evidencia, trazabilidad en `auditoria`); calidad del GPS; privacidad de ubicaciones.
- **Producto:** incentivo a fabricar incidentes para obtener un abogado. Pensar mecanismos de verificación (evidencia, validación humana, antecedentes previos independientes).
