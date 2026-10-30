package co.edu.unicesar.reda.respuesta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReporteDetalleResponse(Integer id, String tipo, String categoria, String zona, String estado,
                                     String descripcion, BigDecimal latitud, BigDecimal longitud,
                                     LocalDate fechaReporte, Short nivelGravedad,
                                     IncidenteResponse incidente, List<CambioEstadoResponse> historial) {
}
