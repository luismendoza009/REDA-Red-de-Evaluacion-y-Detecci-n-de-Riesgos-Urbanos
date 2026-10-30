package co.edu.unicesar.reda.respuesta;

import java.time.LocalDate;

public record NotificacionResponse(Integer id, Integer idReporte, String estadoAnterior, String estadoNuevo,
                                   LocalDate fechaCreacion, boolean leida) {
}
