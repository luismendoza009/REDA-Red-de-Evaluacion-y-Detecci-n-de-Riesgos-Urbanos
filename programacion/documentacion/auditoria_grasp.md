# Auditoría GRASP del backend de REDA

**Alcance:** `programacion/backend` en el commit `1edf98d` (primera versión del backend).
**Corrección:** commit `6493f6d`. El código anterior a la corrección está en `auditorias/antes/`.
**Método:** para cada clase se preguntó *¿quién tiene la información para hacer esto?* (Experto),
*¿quién debería crear este objeto?* (Creador), *¿esta clase hace una sola cosa?* (Cohesión) y
*¿de cuántas clases depende?* (Acoplamiento).

## 1. Patrones aplicados (estado actual)

| Patrón | Dónde | Por qué |
|---|---|---|
| **Experto en información** | `NombreEstado.puedePasarA`, `Reporte.cambiarEstado`, `Reporte.puedeSerVistoPor`, `Usuario.esPersonalDeRevision`, `TipoDano.incluyeLesion` | La responsabilidad se asigna a quien tiene los datos: el estado conoce su ciclo de vida; el reporte conoce a su autor y su estado |
| **Creador** | `Reporte.cambiarEstado` crea `HistorialEstado`; `Reporte.registrarIncidente` crea `Incidente` | El reporte *agrega* su historial y su incidente. Los constructores de esas clases son de paquete: nadie más puede crearlos |
| **Controlador** | `RegistroReporteService`, `ConsultaReporteService`, `RevisionReporteService`, `AuthService` | Cada uno recibe las operaciones de un caso de uso y las coordina. Los `@RestController` solo traducen HTTP |
| **Bajo acoplamiento** | Los controladores dependen de servicios, nunca de repositorios ni de entidades | Un cambio de tabla no llega a la API |
| **Alta cohesión** | Un servicio por caso de uso (corregido, ver hallazgo G1) | Cada clase tiene un motivo para existir |
| **Fabricación pura** | Repositorios, `JwtService`, `ReporteMapper`, `Busqueda` | No son conceptos del negocio; existen para que las entidades no carguen con persistencia, tokens o conversión |
| **Indirección** | `ReporteMapper` y `CasoMapper` entre entidades y respuestas de la API; `BuscadorAntecedentes` entre servicios y SQL | Desacopla dos partes que cambian por motivos distintos |
| **Variaciones protegidas** | `BuscadorAntecedentes` (interfaz) | El punto de cambio previsto (Haversine hoy, PL/pgSQL en la Entrega 3 de BD, PostGIS en el futuro) queda detrás de una interfaz estable |

## 2. Hallazgos y correcciones

### G1 — Baja cohesión en `ReporteService` (Alta cohesión / Controlador)

**Hallazgo.** Un solo `ReporteService` atendía **tres** casos de uso distintos: registrar reportes, consultarlos
y revisarlos (cambiar estado). Tenía **9 dependencias** en el constructor, y una de ellas (`PropiedadesReda`)
solo para leer un número. Un controlador GRASP debe representar *un* caso de uso o subsistema; este era un
"controlador inflado": cualquier cambio en la revisión obligaba a abrir la misma clase que la creación.

**Corrección.** Se dividió en tres controladores de caso de uso:

| Servicio | Dependencias | Responsabilidad |
|---|---|---|
| `RegistroReporteService` | 8 | Crear condiciones e incidentes y vincular antecedentes |
| `ConsultaReporteService` | 3 | Mis reportes, detalle, antecedentes (solo lectura) |
| `RevisionReporteService` | 3 | Cambiar estado |

Antes: `auditorias/antes/ReporteService.java.txt`. Después: `service/RegistroReporteService.java`,
`ConsultaReporteService.java`, `RevisionReporteService.java`.

### G2 — Regla de acceso fuera de su experto (Experto en información)

**Hallazgo.** La regla "el detalle de un reporte solo lo ven su autor o el personal de revisión" estaba en un
método privado del servicio:

```java
// antes, en ReporteService
private static void verificarAcceso(Reporte reporte, Usuario solicitante) {
    if (!reporte.esDe(solicitante) && !solicitante.esPersonalDeRevision()) { throw ... }
}
```

El servicio estaba *preguntándole* al reporte por su autor para decidir por él. Quien tiene la información
(el autor) es el `Reporte`.

**Corrección.** La decisión pasa al dominio; el servicio solo actúa según la respuesta:

```java
// después, en Reporte
public boolean puedeSerVistoPor(Usuario usuario) {
    return esDe(usuario) || usuario.esPersonalDeRevision();
}
// en ConsultaReporteService
if (!reporte.puedeSerVistoPor(solicitante)) { throw new AccesoDenegadoException(...); }
```

Beneficio: la regla se prueba sin Spring ni base de datos (`ReporteTest.elDetalleSoloLoVenElAutorYElPersonalDeRevision`)
y cualquier otro caso de uso que necesite la misma regla la reutiliza.

### G3 — Responsabilidad del radio de búsqueda mal ubicada (Experto / Bajo acoplamiento)

**Hallazgo.** El servicio leía el radio de búsqueda de la configuración y se lo pasaba al buscador en cada
llamada. Así, todo el que buscaba antecedentes tenía que conocer la configuración, y la definición de
"cerca" quedaba repartida.

**Corrección.** El radio es parte de *cómo se busca*, así que lo decide la implementación
(`BuscadorAntecedentesHaversine` lo recibe por constructor). La interfaz quedó con un parámetro menos y los
servicios ya no dependen de `PropiedadesReda`.

## 3. Verificación

- Las 42 pruebas pasan después de la corrección (`mvn verify -Pintegracion`), entre ellas las de integración
  del flujo completo, que no cambiaron: la refactorización no alteró el comportamiento externo.
- Prueba nueva para G2: `ReporteTest.elDetalleSoloLoVenElAutorYElPersonalDeRevision`.
