package co.edu.unicesar.reda.respuesta;

import java.util.List;

public record NotificacionesResponse(List<NotificacionResponse> items, long noLeidas) {
}
