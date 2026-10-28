package co.edu.unicesar.reda.respuesta;

import java.util.List;

/**
 * Lo que el ciudadano ve de un abogado para poder compararlo. El correo de contacto solo viene
 * del abogado que el ciudadano eligió (de los demás postulados no se expone).
 */
public record PerfilPublicoAbogado(Integer id, String nombre, String foto, boolean verificado,
                                   String especialidad, String ciudad,
                                   short aniosExperiencia, String presentacion, long casosTomadosEnReda,
                                   String correoContacto, Double calificacionPromedio, long numCalificaciones,
                                   List<ComentarioPublico> comentarios) {
}
