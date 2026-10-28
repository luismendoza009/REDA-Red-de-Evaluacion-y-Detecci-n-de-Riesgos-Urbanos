package co.edu.unicesar.reda.peticion;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Perfil profesional del abogado: mismas reglas que en el registro. Solo si está verificado. */
public record ActualizarPerfilAbogadoRequest(
        @NotBlank @Size(max = 80) String especialidad,
        @NotBlank @Size(max = 80) String ciudad,
        @NotNull @Min(0) @Max(70) Short aniosExperiencia,
        @Size(max = 600) String presentacion) {
}
