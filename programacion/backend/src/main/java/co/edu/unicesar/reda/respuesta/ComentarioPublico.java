package co.edu.unicesar.reda.respuesta;

import java.time.LocalDate;

/** Comentario de un cliente, sin su nombre: solo el barrio y el mes del caso como contexto. */
public record ComentarioPublico(short puntaje, String comentario, String contexto, LocalDate fecha) {
}
