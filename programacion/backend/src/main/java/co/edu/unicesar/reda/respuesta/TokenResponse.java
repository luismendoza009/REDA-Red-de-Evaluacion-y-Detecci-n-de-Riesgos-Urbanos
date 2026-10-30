package co.edu.unicesar.reda.respuesta;

public record TokenResponse(String token, String tipo, long expiraEnMinutos,
                            Integer idUsuario, String nombre, String rol) {
}
