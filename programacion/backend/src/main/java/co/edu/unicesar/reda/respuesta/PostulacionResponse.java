package co.edu.unicesar.reda.respuesta;

import java.time.OffsetDateTime;

public record PostulacionResponse(Integer id, PerfilPublicoAbogado abogado, String mensaje, String estado,
                                  OffsetDateTime fecha) {
}
