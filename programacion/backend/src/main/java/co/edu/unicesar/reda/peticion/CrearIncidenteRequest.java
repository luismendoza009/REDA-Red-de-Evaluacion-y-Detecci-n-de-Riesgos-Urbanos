package co.edu.unicesar.reda.peticion;

import co.edu.unicesar.reda.model.TipoDano;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;

public record CrearIncidenteRequest(
        @NotNull @Valid FallaRequest falla,
        @NotNull TipoDano tipoDano,
        @NotBlank @Size(min = 10, max = 2000) String descripcionDano,
        @NotNull @PastOrPresent OffsetDateTime fechaHoraHecho,
        boolean huboAtencionMedica,
        Integer idEntidad,
        @Size(max = 5, message = "máximo 5 testigos") List<@Valid TestigoRequest> testigos) {
}
