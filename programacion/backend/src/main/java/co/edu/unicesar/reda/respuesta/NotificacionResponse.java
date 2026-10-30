package co.edu.unicesar.reda.respuesta;

import java.time.OffsetDateTime;

public record NotificacionResponse(Integer id, Integer idReporte, String estadoAnterior, String estadoNuevo,
                                   OffsetDateTime fechaCreacion, boolean leida) {
}
