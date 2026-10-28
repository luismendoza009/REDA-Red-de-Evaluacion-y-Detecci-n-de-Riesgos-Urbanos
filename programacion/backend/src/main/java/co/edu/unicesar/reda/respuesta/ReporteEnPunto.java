package co.edu.unicesar.reda.respuesta;

import java.time.LocalDate;

/** Un reporte dentro de un punto del mapa. */
public record ReporteEnPunto(Integer id, String tipo, String categoria, String estado, String situacion,
                             LocalDate fecha, String descripcion, Short nivelGravedad, long numFotos) {
}
