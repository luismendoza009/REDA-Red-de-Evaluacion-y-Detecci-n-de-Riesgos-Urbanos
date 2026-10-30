package co.edu.unicesar.reda.respuesta;

import java.time.LocalDate;
import java.util.List;

/** Un reporte en la bandeja del operador, con los estados a los que puede pasar. */
public record ReporteEnRevision(Integer id, String tipo, String categoria, String zona, String estado,
                                LocalDate fechaReporte, String descripcion, Short nivelGravedad, int numFotos,
                                List<String> siguientesEstados) {
}
