# Auditoría SOLID del backend de REDA

**Alcance:** `programacion/backend` en el commit `1edf98d`. **Corrección:** commit `6493f6d`.
Código anterior en `auditorias/antes/`.

## Resumen

| Principio | Resultado de la auditoría | Acción |
|---|---|---|
| **S** — Responsabilidad única | ✗ `ReporteService` con 3 responsabilidades | Corregido (S1) |
| **O** — Abierto/cerrado | ✗ El manejador de excepciones se modificaba con cada excepción nueva | Corregido (O1) |
| **L** — Sustitución de Liskov | ✓ Sin hallazgos | Verificado (L1) |
| **I** — Segregación de interfaces | ✗ `ReporteRepository` mezclaba CRUD y mapa público | Corregido (I1) |
| **D** — Inversión de dependencias | ✓ Ya aplicado con `BuscadorAntecedentes`; ✗ una dependencia de configuración innecesaria | Corregido (D1) |

## S1 — `ReporteService` tenía tres motivos para cambiar

**Hallazgo.** Registrar, consultar y revisar reportes estaban en la misma clase (9 dependencias). Son tres
motivos de cambio distintos: un cambio en las reglas de revisión no debería tocar el código de registro.
**Corrección.** `RegistroReporteService`, `ConsultaReporteService` y `RevisionReporteService` (ver G1 en la
auditoría GRASP). El helper repetido "buscar o lanzar 404" pasó a `Busqueda.porId` (DRY).

## O1 — El manejador de excepciones no estaba cerrado a modificación

**Hallazgo.** `ManejadorGlobalExcepciones` tenía un método casi idéntico por cada excepción propia:

```java
// antes (5 métodos así)
@ExceptionHandler(RecursoNoEncontradoException.class)
ResponseEntity<ErrorRespuesta> noEncontrado(RedaException e, HttpServletRequest p) {
    return responder(HttpStatus.NOT_FOUND, e.getCodigo(), e.getMessage(), p);
}
@ExceptionHandler(ReglaNegocioException.class)
ResponseEntity<ErrorRespuesta> reglaNegocio(RedaException e, HttpServletRequest p) {
    return responder(HttpStatus.UNPROCESSABLE_ENTITY, e.getCodigo(), e.getMessage(), p);
}
// ...
```

Agregar una excepción (por ejemplo `CasoCerradoException` en la siguiente iteración) obligaba a modificar
esta clase, y olvidarlo hacía que la excepción saliera como error 500.

**Corrección.** Cada excepción declara su `CategoriaError` (un concepto del dominio, sin HTTP) y un único
manejador traduce la categoría:

```java
// después
public class ReglaNegocioException extends RedaException {
    public ReglaNegocioException(String codigo, String mensaje) {
        super(CategoriaError.REGLA_NEGOCIO, codigo, mensaje);
    }
}

@ExceptionHandler(RedaException.class)
ResponseEntity<ErrorRespuesta> propia(RedaException e, HttpServletRequest p) {
    return responder(estadoHttp(e.getCategoria()), e.getCodigo(), e.getMessage(), p);
}
```

Una excepción nueva solo elige su categoría: el manejador no cambia. El `switch` sobre el `enum` no
compila si se agrega una categoría sin traducir, así que el olvido no puede llegar a producción.
Prueba: `ManejadorGlobalExcepcionesTest`.

## L1 — Liskov (sin hallazgos)

Se revisaron las dos jerarquías del proyecto:
- `RedaException` y sus 5 subclases: todas respetan el contrato de la clase base (mensaje + código +
  categoría) y el manejador las trata a todas por igual, sin `instanceof`.
- `BuscadorAntecedentes`: la implementación Haversine cumple el contrato documentado (misma categoría,
  radio, fecha, no rechazados). En las pruebas unitarias se sustituye por un doble de Mockito y el servicio
  funciona igual: eso es Liskov en la práctica.

## I1 — `ReporteRepository` forzaba a sus clientes a depender de lo que no usan

**Hallazgo.** Además del CRUD de reportes, `ReporteRepository` exponía `mapaPublico()`, una consulta sobre la
vista `v_mapa_publico` con su propia proyección. `MapaPublicoService` dependía de todo el CRUD de reportes
solo para pintar el mapa, y los servicios de reportes veían una consulta pública que no usan.

**Corrección.** La consulta pasó a `MapaPublicoRepository` (SQL explícito con `JdbcTemplate`) y
`ReporteRepository` quedó solo con lo que usan los servicios de reportes.

## D1 — Dependencia de la configuración completa para leer un número

**Hallazgo.** `ReporteService` recibía `PropiedadesReda` (toda la configuración: JWT, CORS…) solo para leer
el radio de antecedentes. Dependía de un detalle de infraestructura que no necesitaba.
**Corrección.** El radio lo decide la implementación del buscador. Los servicios dependen únicamente de la
abstracción `BuscadorAntecedentes` (DIP), que es lo que permitió probar `RegistroReporteService` sin base de
datos (`RegistroReporteServiceTest`).

## Verificación

42 pruebas en verde antes y después de la corrección; las 4 de integración no se modificaron, lo que demuestra
que la refactorización no cambió el comportamiento observable de la API.
