package co.edu.unicesar.reda.respuesta;

/** Barrio sugerido para el punto que el ciudadano marcó en el mapa. */
public record ZonaSugeridaResponse(Integer id, String nombre, int distanciaMetros) {
}
