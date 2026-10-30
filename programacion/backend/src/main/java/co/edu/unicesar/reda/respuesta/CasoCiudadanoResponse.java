package co.edu.unicesar.reda.respuesta;

import java.time.LocalDate;
import java.util.List;

/** Vista del ciudadano: su caso y quiénes se postularon. */
public record CasoCiudadanoResponse(Integer id, Integer idReporte, String categoria, String zona, String tipoDano,
                                    String estado, LocalDate fechaCreacion, PerfilPublicoAbogado abogadoElegido,
                                    List<PostulacionResponse> postulaciones, boolean puedeCalificar,
                                    Short miCalificacion, String miComentario) {
}
