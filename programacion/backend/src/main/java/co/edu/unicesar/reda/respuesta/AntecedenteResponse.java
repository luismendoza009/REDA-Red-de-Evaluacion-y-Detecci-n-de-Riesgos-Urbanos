package co.edu.unicesar.reda.respuesta;

import java.time.LocalDate;

public record AntecedenteResponse(Integer idReporte, LocalDate fechaReporte, String estado,
                                  String descripcion, double distanciaMetros) {
}
