# Decisiones de diseño del backend — REDA (Programación 3)

| # | Decisión | Por qué | Alternativa descartada |
|---|---|---|---|
| P01 | Java 17 + Spring Boot 3.5 + Maven | Java 17 es la versión LTS instalada en el equipo; Spring Boot da inyección de dependencias, web, seguridad y JPA integrados | Java EE "a mano"; Spring Boot 4 (más reciente, menos material de referencia) |
| P02 | Arquitectura en capas `controller / service / model / repository` + `dto / exception / security / config` | Tema del curso; cada capa con un motivo de cambio | MVC con lógica en controladores |
| P03 | Nombres del dominio en **español** (`Reporte`, `cambiarEstado`), sufijos técnicos de Spring en inglés (`Controller`, `Service`, `Repository`) **(supuesto)** | Coincide con las tablas y el diccionario de datos; los sufijos son la convención del framework | Todo en inglés (rompe la correspondencia con la base) |
| P04 | JPA para el CRUD + **SQL explícito con JdbcTemplate** para el buscador de antecedentes y el mapa | Las consultas importantes del curso de BD se ven tal cual, parametrizadas | Todo con JPQL |
| P05 | `ddl-auto=validate`: Hibernate **nunca** crea tablas | El esquema lo dan las migraciones de `base-de-datos/`; `validate` detiene el arranque si Java y SQL no coinciden | `update` (Hibernate modificaría el esquema a escondidas) |
| P06 | Entidades sin setters; fábricas y métodos de negocio | Encapsulamiento: el estado solo cambia por operaciones válidas | Entidades "anémicas" con getters/setters |
| P07 | Validación en tres niveles: DTO (Bean Validation), dominio, base | Cada nivel atrapa lo suyo; la base es la última barrera | Validar solo en el frontend o solo en la base |
| P08 | Excepciones propias no verificadas con `CategoriaError` + un manejador global | Formato de error único; abierto a nuevas excepciones (ver auditoría SOLID, O1) | `try/catch` en cada controlador |
| P09 | JWT sin sesión en el servidor; secreto obligatorio por variable de entorno, validado al arrancar | La API no guarda estado; ningún secreto en el repositorio | Sesiones con cookie (requieren CSRF) |
| P10 | Contraseñas con BCrypt (costo 10); el registro público solo crea CIUDADANOS | Las cuentas con privilegios no se pueden autocrear | — |
| P11 | En un cambio de estado se usa `flush()`, no `save()` | **Error real encontrado en las pruebas:** `save()` sobre una entidad ya gestionada hace `merge`, que no propagaba la inserción del historial (fallaba con `id_reporte` nulo). La entidad ya está gestionada; `flush()` basta | `save()` + `CascadeType.MERGE` |
| P12 | Los reportes **rechazados** no cuentan como antecedentes | Detectado al probar el flujo de la demo; misma regla en la base (ver `base-de-datos/documentacion/decisiones.md`, D18) | — |
| P13 | Pruebas de integración contra PostgreSQL real, con `@Transactional` para deshacer los cambios | El equipo no tiene Docker (Testcontainers); H2 no soporta las vistas ni el SQL de PostgreSQL | H2 en memoria |
| P14 | El mapa público lee una **vista** de la base que ya oculta los datos personales | La privacidad no depende de que el código Java "se acuerde" de ocultar campos | Filtrar campos en Java |

## Pendiente (siguientes iteraciones)

- Carga de fotos (evidencias) con validación de tipo, tamaño y hash SHA-256.
- Casos y remisiones a abogados; vista del abogado.
- Cálculo de IRU e IVC (depende de las funciones PL/pgSQL de la Entrega 3 de BD).
- Dashboard y frontend (tecnología por decidir; los temas del curso no incluyen frontend).
