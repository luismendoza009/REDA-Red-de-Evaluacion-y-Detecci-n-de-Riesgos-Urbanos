package co.edu.unicesar.reda.respuesta;

import java.time.LocalDate;
import java.util.List;

/** Detalle público de un reporte: su línea de tiempo y cuántas fotos privadas tiene. */
public record DetallePublicoResponse(Integer id, String tipo, String categoria, String zona, String estado,
                                     String situacion, LocalDate fecha, String descripcion, Short nivelGravedad,
                                     long numFotos, List<Hito> lineaDeTiempo) {
}
