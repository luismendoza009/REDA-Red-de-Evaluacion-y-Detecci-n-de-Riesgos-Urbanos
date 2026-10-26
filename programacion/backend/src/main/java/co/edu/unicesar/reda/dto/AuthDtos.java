package co.edu.unicesar.reda.dto;

import jakarta.validation.constraints.*;

/** Contratos de registro e inicio de sesión. */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegistroRequest(
            @NotBlank @Size(max = 120) String nombre,
            @NotBlank @Email @Size(max = 150) String correo,
            // BCrypt solo usa los primeros 72 bytes: se limita para que no haya sorpresas
            @NotBlank @Size(min = 8, max = 72, message = "debe tener entre 8 y 72 caracteres") String password,
            @AssertTrue(message = "debe aceptar la política de tratamiento de datos") boolean aceptaTratamientoDatos) {
    }

    public record LoginRequest(
            @NotBlank @Email String correo,
            @NotBlank String password) {
    }

    public record TokenResponse(String token, String tipo, long expiraEnMinutos,
                                Integer idUsuario, String nombre, String rol) {
    }
}
