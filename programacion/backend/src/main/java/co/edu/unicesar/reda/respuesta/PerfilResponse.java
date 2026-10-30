package co.edu.unicesar.reda.respuesta;

import java.time.LocalDate;

/** {@code foto}: ruta para descargar la foto de perfil (con sesión), o null si no tiene. */
public record PerfilResponse(Integer id, String nombre, String correo, String rol, LocalDate fechaRegistro,
                             String foto, PerfilAbogado abogado) {
}
