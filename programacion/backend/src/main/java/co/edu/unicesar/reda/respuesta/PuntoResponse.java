package co.edu.unicesar.reda.respuesta;

import java.time.LocalDate;
import java.util.List;

/** Un punto del mapa: uno o varios reportes a menos del radio de agrupación. */
public record PuntoResponse(Integer id, double latitud, double longitud, String zona, String frecuencia,
                            int totalReportes, int incidentes, List<String> categorias,
                            LocalDate primerReporte, LocalDate ultimoReporte,
                            ConteoSituaciones situaciones, List<ReporteEnPunto> reportes) {
}
