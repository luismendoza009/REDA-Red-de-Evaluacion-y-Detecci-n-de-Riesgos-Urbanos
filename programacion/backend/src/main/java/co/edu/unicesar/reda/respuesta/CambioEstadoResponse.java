package co.edu.unicesar.reda.respuesta;

import java.time.LocalDate;

public record CambioEstadoResponse(String estadoAnterior, String estadoNuevo, LocalDate fecha,
                                   String responsable, String observacion) {
}
