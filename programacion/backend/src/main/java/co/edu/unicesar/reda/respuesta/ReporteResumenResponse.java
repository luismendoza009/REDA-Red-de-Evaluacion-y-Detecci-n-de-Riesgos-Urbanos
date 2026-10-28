package co.edu.unicesar.reda.respuesta;

import java.time.OffsetDateTime;

public record ReporteResumenResponse(Integer id, String tipo, String categoria, String zona, String estado,
                                     OffsetDateTime fechaReporte, Short nivelGravedad) {
}
