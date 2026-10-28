# Mapa de temas de Programación 3 → dónde están en el código

Rutas relativas a `programacion/backend/src/main/java/co/edu/unicesar/reda/` salvo que se indique otra cosa.

## Estructura del backend (una carpeta por capa)

```
co.edu.unicesar.reda
├─ controller/   Capa de presentación: recibe HTTP, valida la petición y delega al servicio
├─ peticion/     Lo que ENTRA a la API: un record por formulario, con sus validaciones (@NotBlank, @Size...)
├─ respuesta/    Lo que SALE de la API en JSON: un record por respuesta, solo con lo que ese rol puede ver
├─ service/      Capa de negocio: un servicio por caso de uso (registrar, consultar, revisar...)
├─ model/        Dominio: entidades con comportamiento (reglas de negocio) y enumeraciones
├─ repository/   Acceso a datos: Spring Data JPA y SQL parametrizado con JdbcTemplate
├─ exception/    Jerarquía de excepciones propia + manejador global único + formato de error
├─ security/     JWT, BCrypt, límite de intentos, permisos por rol
└─ config/       Configuración (propiedades de REDA, Swagger)
```

Flujo de una petición: `controller` → `service` → `model` + `repository` → PostgreSQL. La respuesta vuelve
convertida en un objeto de `respuesta/` (lo construyen `ReporteMapper` y `CasoMapper`).

## Temas

| Tema visto en clase | Dónde se ve en REDA | Qué mostrar en la sustentación |
|---|---|---|
| **Oh My Posh** | `programacion/documentacion/entorno_desarrollo.md` | Configuración de la terminal del equipo (PowerShell) para trabajar con Git, Java y Maven |
| **POO** | `model/`, `exception/` | Encapsulamiento: entidades sin setters, constructores `protected`, fábricas con nombre de negocio (`Reporte.nuevo`, `Usuario.registrar`). Herencia y polimorfismo: `RedaException` (abstracta) y sus 6 subclases. Abstracción: interfaces `BuscadorAntecedentes`, `AlmacenamientoArchivos`, `EnvioCorreo`. Enumeraciones con comportamiento (`NombreEstado.puedePasarA`, `TipoDano.incluyeLesion`). Records inmutables en `peticion/` y `respuesta/` |
| **Arquitectura en capas** | Estructura de arriba | Cada capa solo conoce a la de abajo. Los controladores no tocan repositorios. La API recibe `peticion/` y devuelve `respuesta/`, nunca las entidades: así un cambio de tabla no cambia el JSON y no se exponen datos privados |
| **Integración** | `src/test/.../controller/*IT.java`; `spring.jpa.hibernate.ddl-auto=validate` | Pruebas que recorren HTTP → servicio → JPA → PostgreSQL real. `validate` impide arrancar si las entidades Java no coinciden con las tablas de las migraciones. La interfaz web (`resources/static`) se integra con la API por HTTP/JSON |
| **Validaciones** | `peticion/` (formato), `model/Reporte.java` e `Incidente.java` (negocio), migraciones SQL (integridad) | Tres niveles: cada uno atrapa lo suyo. Ejemplo: "atención médica sin lesión" se valida en el dominio Y en la base (`ck_incidentes_atencion`). La política de contraseñas está una sola vez en `peticion/PoliticaPassword.java` |
| **Excepciones** | `exception/` | Jerarquía propia con código estable y `CategoriaError`; **un único** `@RestControllerAdvice` (`ManejadorGlobalExcepciones`); formato de error único (`ErrorRespuesta`); los errores 500 nunca muestran detalles internos |
| **Código limpio** | Todo el backend | Nombres del dominio en español, un archivo por clase, métodos cortos con una sola tarea, sin números mágicos (`METROS_POR_GRADO`), sin duplicación (`Busqueda.porId`, `FallaRequest` reutilizado, `PoliticaPassword`), comentarios que explican el *por qué* |
| **Patrones GRASP** | Ver `auditoria_grasp.md` | Experto, Creador, Controlador, Bajo acoplamiento, Alta cohesión, Fabricación pura, Indirección, Variaciones protegidas |
| **Auditoría y corrección GRASP** | `auditoria_grasp.md` + commit `6493f6d` | Hallazgo → corrección, con el código de antes en `auditorias/antes/` |
| **Auditoría y corrección SOLID** | `auditoria_solid.md` + commit `6493f6d` | Hallazgo → corrección para S, O, I y D; L verificado |

## Cómo comprobar que todo funciona

```powershell
cd programacion\backend
mvn test                                 # 81 pruebas unitarias, sin base de datos
$env:REDA_DB_CLAVE = "<clave>"; $env:REDA_JWT_SECRETO = "<secreto de 32+ caracteres>"
mvn verify -Pintegracion                 # + 17 pruebas de integración con PostgreSQL
.\iniciar.ps1                            # arranca la aplicación en http://localhost:8080
```
