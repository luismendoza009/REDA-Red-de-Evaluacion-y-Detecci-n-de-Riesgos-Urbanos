package co.edu.unicesar.reda.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/** Contratos del flujo de casos: el ciudadano pide abogado, los abogados se postulan y el ciudadano elige. */
public final class CasoDtos {

    private CasoDtos() {
    }

    public record SolicitarAbogadoRequest(
            @NotNull Integer idReporte,
            @AssertTrue(message = "debe autorizar que los abogados vean el resumen anónimo de su caso") boolean autorizaCompartirResumen) {
    }

    public record PostularRequest(@NotBlank @Size(min = 20, max = 1500) String mensaje) {
    }

    public record ElegirRequest(@NotNull Integer idPostulacion) {
    }

    public record CerrarRequest(@NotBlank @Size(min = 10, max = 1000) String observacion) {
    }

    /**
     * Lo que el ciudadano ve de un abogado para poder compararlo. El correo de contacto solo viene
     * del abogado que el ciudadano eligió (de los demás postulados no se expone).
     */
    public record PerfilPublicoAbogado(Integer id, String nombre, String especialidad, String ciudad,
                                       short aniosExperiencia, String presentacion, long casosTomadosEnReda,
                                       String correoContacto, Double calificacionPromedio, long numCalificaciones,
                                       List<ComentarioPublico> comentarios) {
    }

    /** Comentario de un cliente, sin su nombre: solo el barrio y el mes del caso como contexto. */
    public record ComentarioPublico(short puntaje, String comentario, String contexto, OffsetDateTime fecha) {
    }

    public record CalificarRequest(
            @NotNull @jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(5) Short puntaje,
            @Size(min = 3, max = 1000) String comentario) {
    }

    public record PostulacionResponse(Integer id, PerfilPublicoAbogado abogado, String mensaje, String estado,
                                      OffsetDateTime fecha) {
    }

    /** Vista del ciudadano: su caso y quiénes se postularon. */
    public record CasoCiudadanoResponse(Integer id, Integer idReporte, String categoria, String zona, String tipoDano,
                                        String estado, OffsetDateTime fechaCreacion, PerfilPublicoAbogado abogadoElegido,
                                        List<PostulacionResponse> postulaciones, boolean puedeCalificar,
                                        Short miCalificacion, String miComentario) {
    }

    /**
     * Vista del abogado ANTES de ser elegido: resumen anónimo. Sin nombre del ciudadano, sin
     * descripción del daño (puede tener datos de salud), sin fotos ni testigos.
     */
    public record CasoDisponibleResponse(Integer id, String categoria, String zona, String entidadResponsable,
                                         OffsetDateTime fechaHecho, String tipoDano, boolean huboAtencionMedica,
                                         int antecedentes, Long diasConocimientoPrevio, int testigos, int fotos,
                                         BigDecimal tarifa, int postulados, String miPostulacion,
                                         OffsetDateTime fechaPublicacion) {
    }

    public record TestigoResponse(String nombre, String contacto) {
    }

    /** Expediente COMPLETO: solo lo ve el abogado que el ciudadano eligió (al elegirlo lo autorizó). */
    public record ExpedienteResponse(Integer id, String estado, OffsetDateTime fechaCreacion, OffsetDateTime fechaCierre,
                                     String observacion, String nombreCiudadano, String correoCiudadano,
                                     ReporteDtos.ReporteDetalleResponse reporte,
                                     List<ReporteDtos.AntecedenteResponse> antecedentes,
                                     List<TestigoResponse> testigos, BigDecimal valorCobrado) {
    }
}
