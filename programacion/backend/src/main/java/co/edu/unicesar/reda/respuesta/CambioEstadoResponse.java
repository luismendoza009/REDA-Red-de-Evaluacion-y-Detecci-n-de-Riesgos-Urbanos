package co.edu.unicesar.reda.respuesta;

import java.time.OffsetDateTime;

public record CambioEstadoResponse(String estadoAnterior, String estadoNuevo, OffsetDateTime fecha,
                                   String responsable, String observacion) {
}
