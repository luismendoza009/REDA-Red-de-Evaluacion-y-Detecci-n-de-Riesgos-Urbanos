package co.edu.unicesar.reda.respuesta;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Vista del abogado ANTES de ser elegido: resumen anónimo. Sin nombre del ciudadano, sin
 * descripción del daño (puede tener datos de salud), sin fotos ni testigos.
 */
public record CasoDisponibleResponse(Integer id, String categoria, String zona, String entidadResponsable,
                                     LocalDate fechaHecho, String tipoDano, boolean huboAtencionMedica,
                                     int antecedentes, Long diasConocimientoPrevio, int testigos, int fotos,
                                     BigDecimal tarifa, int postulados, String miPostulacion,
                                     LocalDate fechaPublicacion) {
}
