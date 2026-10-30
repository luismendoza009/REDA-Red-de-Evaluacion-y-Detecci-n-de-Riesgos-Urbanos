package co.edu.unicesar.reda.peticion;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank @Email @Size(max = 150) String correo,
        // BCrypt solo usa los primeros 72 bytes: se limita para que no haya sorpresas
        @NotBlank @Pattern(regexp = PoliticaPassword.PATRON, message = PoliticaPassword.MENSAJE) String password,
        @AssertTrue(message = "debe aceptar los términos y la política de tratamiento de datos") boolean aceptaTratamientoDatos) {
}
