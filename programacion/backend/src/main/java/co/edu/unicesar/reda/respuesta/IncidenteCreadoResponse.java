package co.edu.unicesar.reda.respuesta;

import java.util.List;

/** Respuesta al crear un incidente: incluye los antecedentes que el sistema encontró y vinculó. */
public record IncidenteCreadoResponse(ReporteDetalleResponse reporte, List<AntecedenteResponse> antecedentes,
                                      Long diasConocimientoPrevio) {
}
