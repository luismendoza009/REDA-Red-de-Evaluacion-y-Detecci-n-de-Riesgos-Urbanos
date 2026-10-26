package co.edu.unicesar.reda.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Propiedades "reda.*" de application.properties, tipadas y VALIDADAS al arrancar:
 * si falta el secreto JWT o es corto, la aplicación no inicia (falla rápido y con mensaje claro,
 * en vez de arrancar insegura).
 */
@Validated
@ConfigurationProperties(prefix = "reda")
public record PropiedadesReda(@Valid Jwt jwt, @Valid Antecedentes antecedentes, @Valid Cors cors) {

    public record Jwt(
            @NotBlank(message = "defina la variable de entorno REDA_JWT_SECRETO")
            @Size(min = 32, message = "el secreto JWT debe tener al menos 32 caracteres (256 bits)") String secreto,
            @Min(1) long minutosExpiracion) {
    }

    public record Antecedentes(@Min(1) int radioMetros) {
    }

    public record Cors(@NotEmpty List<String> origenes) {
    }
}
