package co.edu.unicesar.reda.peticion;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Datos comunes a toda falla. Se reutilizan en condición e incidente (sin duplicar validaciones). */
public record FallaRequest(
        @NotNull Integer idCategoria,
        @NotNull Integer idZona,
        @NotBlank @Size(min = 10, max = 2000) String descripcion,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") @Digits(integer = 2, fraction = 6) BigDecimal latitud,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") @Digits(integer = 3, fraction = 6) BigDecimal longitud,
        @Min(1) @Max(5) Short nivelGravedad) {
}
