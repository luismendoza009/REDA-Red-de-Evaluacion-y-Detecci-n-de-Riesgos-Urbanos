package co.edu.unicesar.reda.respuesta;

import java.time.LocalDate;

/** Un reporte en "Mis reportes", con la situación de su caso si lo tiene. */
public record MiReporteResponse(Integer id, String tipo, String categoria, String zona, String estado,
                                LocalDate fechaReporte, String descripcion, int numFotos,
                                boolean puedeBuscarAbogado, Integer idCaso, String estadoCaso) {
}
