package co.edu.unicesar.reda.peticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActualizarPerfilRequest(@NotBlank @Size(max = 120) String nombre) {
}
