package co.edu.unicesar.reda.respuesta;

import java.time.LocalDate;

public record PostulacionResponse(Integer id, PerfilPublicoAbogado abogado, String mensaje, String estado,
                                  LocalDate fecha) {
}
