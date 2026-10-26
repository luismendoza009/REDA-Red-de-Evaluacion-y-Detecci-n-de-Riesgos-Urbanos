package co.edu.unicesar.reda.dto;

import jakarta.validation.constraints.*;

/** Contratos de cuentas: registro, inicio de sesión, recuperación y perfil. */
public final class AuthDtos {

    /** Política de contraseñas, definida una sola vez: 8 a 72 caracteres, con al menos una letra y un número. */
    public static final String PATRON_PASSWORD = "^(?=.*\\p{L})(?=.*\\d).{8,72}$";
    public static final String MENSAJE_PASSWORD = "debe tener entre 8 y 72 caracteres, con al menos una letra y un número";

    private AuthDtos() {
    }

    public record RegistroRequest(
            @NotBlank @Size(max = 120) String nombre,
            @NotBlank @Email @Size(max = 150) String correo,
            // BCrypt solo usa los primeros 72 bytes: se limita para que no haya sorpresas
            @NotBlank @Pattern(regexp = PATRON_PASSWORD, message = MENSAJE_PASSWORD) String password,
            @AssertTrue(message = "debe aceptar los términos y la política de tratamiento de datos") boolean aceptaTratamientoDatos) {
    }

    /** Un abogado solicita vincularse: queda pendiente hasta que se verifique su tarjeta profesional. */
    public record RegistroAbogadoRequest(
            @NotBlank @Size(max = 120) String nombre,
            @NotBlank @Email @Size(max = 150) String correo,
            @NotBlank @Pattern(regexp = PATRON_PASSWORD, message = MENSAJE_PASSWORD) String password,
            @NotBlank @Pattern(regexp = "^[0-9]{4,10}$", message = "debe tener entre 4 y 10 dígitos") String tarjetaProfesional,
            @NotBlank @Size(max = 80) String especialidad,
            @NotBlank @Size(max = 80) String ciudad,
            @NotNull @Min(0) @Max(70) Short aniosExperiencia,
            @Size(max = 600) String presentacion,
            @AssertTrue(message = "debe aceptar los términos y la política de tratamiento de datos") boolean aceptaTratamientoDatos) {
    }

    public record LoginRequest(
            @NotBlank @Email String correo,
            @NotBlank String password) {
    }

    public record TokenResponse(String token, String tipo, long expiraEnMinutos,
                                Integer idUsuario, String nombre, String rol) {
    }

    public record RecuperarRequest(@NotBlank @Email String correo) {
    }

    public record RestablecerRequest(
            @NotBlank @Size(min = 20, max = 200) String token,
            @NotBlank @Pattern(regexp = PATRON_PASSWORD, message = MENSAJE_PASSWORD) String nuevaPassword) {
    }

    public record MensajeResponse(String mensaje) {
    }

    public record PerfilResponse(Integer id, String nombre, String correo, String rol, java.time.OffsetDateTime fechaRegistro,
                                 PerfilAbogado abogado) {
    }

    public record PerfilAbogado(String tarjetaProfesional, String especialidad, String ciudad, short aniosExperiencia,
                                String presentacion, boolean verificado) {
    }

    public record ActualizarPerfilRequest(@NotBlank @Size(max = 120) String nombre) {
    }

    public record CambioPasswordRequest(
            @NotBlank String actual,
            @NotBlank @Pattern(regexp = PATRON_PASSWORD, message = MENSAJE_PASSWORD) String nueva) {
    }
}
