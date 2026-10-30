package co.edu.unicesar.reda.respuesta;

import java.time.LocalDate;

public record ReporteResumenResponse(Integer id, String tipo, String categoria, String zona, String estado,
                                     LocalDate fechaReporte, Short nivelGravedad) {
}
