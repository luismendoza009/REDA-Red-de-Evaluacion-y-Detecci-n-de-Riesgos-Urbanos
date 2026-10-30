package co.edu.unicesar.reda.peticion;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Un abogado solicita vincularse: queda pendiente hasta que se verifique su tarjeta profesional. */
public record RegistroAbogadoRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank @Email @Size(max = 150) String correo,
        @NotBlank @Pattern(regexp = PoliticaPassword.PATRON, message = PoliticaPassword.MENSAJE) String password,
        @NotBlank @Pattern(regexp = "^[0-9]{4,10}$", message = "debe tener entre 4 y 10 dígitos") String tarjetaProfesional,
        @NotBlank @Size(max = 80) String especialidad,
        @NotBlank @Size(max = 80) String ciudad,
        @NotNull @Min(0) @Max(70) Short aniosExperiencia,
        @Size(max = 600) String presentacion,
        @AssertTrue(message = "debe aceptar los términos y la política de tratamiento de datos") boolean aceptaTratamientoDatos) {
}
