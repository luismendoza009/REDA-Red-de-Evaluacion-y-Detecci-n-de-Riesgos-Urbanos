package co.edu.unicesar.reda.peticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CambioPasswordRequest(
        @NotBlank String actual,
        @NotBlank @Pattern(regexp = PoliticaPassword.PATRON, message = PoliticaPassword.MENSAJE) String nueva) {
}
