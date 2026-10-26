package co.edu.unicesar.reda.dto;

import co.edu.unicesar.reda.model.NombreEstado;
import co.edu.unicesar.reda.model.TipoDano;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Contratos de la API de reportes.
 * Validación en tres niveles (cada uno atrapa lo que le corresponde):
 *   1. Formato -> aquí, con Bean Validation (rango de coordenadas, longitudes, obligatorios).
 *   2. Negocio -> en el dominio (transiciones de estado, atención médica solo con lesión...).
 *   3. Integridad -> en PostgreSQL (CHECK, FK, UNIQUE), la última barrera.
 */
public final class ReporteDtos {

    private ReporteDtos() {
    }

    /** Datos comunes a toda falla. Se reutilizan en condición e incidente (sin duplicar validaciones). */
    public record FallaRequest(
            @NotNull Integer idCategoria,
            @NotNull Integer idZona,
            @NotBlank @Size(min = 10, max = 2000) String descripcion,
            @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") @Digits(integer = 2, fraction = 6) BigDecimal latitud,
            @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") @Digits(integer = 3, fraction = 6) BigDecimal longitud,
            @Min(1) @Max(5) Short nivelGravedad) {
    }

    public record CrearIncidenteRequest(
            @NotNull @Valid FallaRequest falla,
            @NotNull TipoDano tipoDano,
            @NotBlank @Size(min = 10, max = 2000) String descripcionDano,
            @NotNull @PastOrPresent OffsetDateTime fechaHoraHecho,
            boolean huboAtencionMedica,
            Integer idEntidad) {
    }

    public record CambioEstadoRequest(
            @NotNull NombreEstado nuevoEstado,
            @Size(max = 1000) String observacion) {
    }

    public record ReporteResumenResponse(Integer id, String tipo, String categoria, String zona, String estado,
                                         OffsetDateTime fechaReporte, Short nivelGravedad) {
    }

    public record CambioEstadoResponse(String estadoAnterior, String estadoNuevo, OffsetDateTime fecha,
                                       String responsable, String observacion) {
    }

    public record IncidenteResponse(Integer id, String tipoDano, String descripcionDano, OffsetDateTime fechaHoraHecho,
                                    boolean huboAtencionMedica, String entidadResponsable) {
    }

    public record AntecedenteResponse(Integer idReporte, OffsetDateTime fechaReporte, String estado,
                                      String descripcion, double distanciaMetros) {
    }

    public record ReporteDetalleResponse(Integer id, String tipo, String categoria, String zona, String estado,
                                         String descripcion, BigDecimal latitud, BigDecimal longitud,
                                         OffsetDateTime fechaReporte, Short nivelGravedad,
                                         IncidenteResponse incidente, List<CambioEstadoResponse> historial) {
    }

    /** Respuesta al crear un incidente: incluye los antecedentes que el sistema encontró y vinculó. */
    public record IncidenteCreadoResponse(ReporteDetalleResponse reporte, List<AntecedenteResponse> antecedentes,
                                          Long diasConocimientoPrevio) {
    }

    /** Punto del mapa público: sin autor ni datos del daño. */
    public record PuntoMapaResponse(Integer idReporte, String tipo, String categoria, String zona, String estado,
                                    BigDecimal latitud, BigDecimal longitud, LocalDate fecha, Short nivelGravedad,
                                    long numEvidencias) {
    }
}
