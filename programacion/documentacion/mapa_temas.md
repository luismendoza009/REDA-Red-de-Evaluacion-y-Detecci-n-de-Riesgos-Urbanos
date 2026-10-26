# Mapa de temas de Programación 3 → dónde están en el código

Rutas relativas a `programacion/backend/src/main/java/co/edu/unicesar/reda/` salvo que se indique otra cosa.

| Tema visto en clase | Dónde se ve en REDA | Qué mostrar en la sustentación |
|---|---|---|
| **Oh My Posh** | `programacion/documentacion/entorno_desarrollo.md` | Configuración de la terminal del equipo (PowerShell) para trabajar con Git, Java y Maven |
| **POO** | `model/` | Encapsulamiento: entidades sin setters, constructores `protected`, fábricas con nombre de negocio (`Reporte.nuevo`, `Usuario.registrar`). Polimorfismo: jerarquía `RedaException`. Enumeraciones con comportamiento (`NombreEstado.puedePasarA`) |
| **Arquitectura en capas** | `controller/` → `service/` → `model/` + `repository/` | Cada capa solo conoce a la de abajo. Los controladores no tocan repositorios; los DTO (`dto/`) separan la API de las entidades |
| **Integración** | `src/test/.../controller/FlujoReporteIT.java`; `spring.jpa.hibernate.ddl-auto=validate` | Prueba que recorre HTTP → servicio → JPA → PostgreSQL real. `validate` impide arrancar si las entidades Java no coinciden con las tablas de las migraciones |
| **Validaciones** | `dto/ReporteDtos.java` (formato), `model/Reporte.java` e `Incidente.java` (negocio), migraciones SQL (integridad) | Tres niveles: cada uno atrapa lo suyo. Ejemplo: "atención médica sin lesión" se valida en el dominio Y en la base (`ck_incidentes_atencion`) |
| **Excepciones** | `exception/` | Jerarquía propia con código estable y categoría; **un único** `@RestControllerAdvice`; formato de error único (`ErrorRespuesta`); los 500 nunca muestran detalles internos |
| **Código limpio** | Todo el backend | Nombres del dominio en español, métodos cortos con una sola tarea, sin números mágicos (`METROS_POR_GRADO`), sin duplicación (`Busqueda.porId`, `FallaRequest` reutilizado), comentarios que explican el *por qué* |
| **Patrones GRASP** | Ver `auditoria_grasp.md` | Experto, Creador, Controlador, Bajo acoplamiento, Alta cohesión, Fabricación pura, Indirección, Variaciones protegidas |
| **Auditoría y corrección GRASP** | `auditoria_grasp.md` + commit `6493f6d` | Hallazgo → corrección, con el código de antes en `auditorias/antes/` |
| **Auditoría y corrección SOLID** | `auditoria_solid.md` + commit `6493f6d` | Hallazgo → corrección para S, O, I y D; L verificado |

## Cómo comprobar que todo funciona

```powershell
cd programacion\backend
mvn test                                 # 38 pruebas unitarias, sin base de datos
$env:REDA_DB_CLAVE = "<clave>"; mvn verify -Pintegracion   # + 4 pruebas de integración con PostgreSQL
```
