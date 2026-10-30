package co.edu.unicesar.reda.respuesta;

import java.time.OffsetDateTime;

public record AntecedenteResponse(Integer idReporte, OffsetDateTime fechaReporte, String estado,
                                  String descripcion, double distanciaMetros) {
}
