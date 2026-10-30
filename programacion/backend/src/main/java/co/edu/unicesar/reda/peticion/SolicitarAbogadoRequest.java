package co.edu.unicesar.reda.peticion;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record SolicitarAbogadoRequest(
        @NotNull Integer idReporte,
        @AssertTrue(message = "debe autorizar que los abogados vean el resumen anónimo de su caso") boolean autorizaCompartirResumen) {
}
