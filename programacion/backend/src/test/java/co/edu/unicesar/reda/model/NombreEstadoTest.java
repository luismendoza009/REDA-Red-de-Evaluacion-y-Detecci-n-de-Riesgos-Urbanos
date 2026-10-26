package co.edu.unicesar.reda.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static co.edu.unicesar.reda.model.NombreEstado.*;
import static org.junit.jupiter.api.Assertions.*;

class NombreEstadoTest {

    @ParameterizedTest(name = "{0} -> {1} permitido")
    @CsvSource({
            "PENDIENTE, EN_REVISION", "PENDIENTE, RECHAZADO",
            "EN_REVISION, VALIDADO", "EN_REVISION, RECHAZADO",
            "VALIDADO, EN_PROCESO", "EN_PROCESO, SOLUCIONADO",
            "RECHAZADO, EN_REVISION"})
    void transicionesPermitidas(NombreEstado desde, NombreEstado hacia) {
        assertTrue(desde.puedePasarA(hacia));
    }

    @ParameterizedTest(name = "{0} -> {1} prohibido")
    @CsvSource({
            "PENDIENTE, VALIDADO",        // no se puede validar sin revisar
            "PENDIENTE, SOLUCIONADO",
            "VALIDADO, RECHAZADO",        // lo validado ya fue verificado
            "EN_PROCESO, PENDIENTE",
            "SOLUCIONADO, EN_PROCESO",    // estado final
            "PENDIENTE, PENDIENTE"})      // un "cambio" al mismo estado no es un cambio
    void transicionesProhibidas(NombreEstado desde, NombreEstado hacia) {
        assertFalse(desde.puedePasarA(hacia));
    }

    @Test
    void solucionadoEsEstadoFinal() {
        assertTrue(SOLUCIONADO.siguientesPermitidos().isEmpty());
    }

    @Test
    void lasEtiquetasCoincidenConLaTablaEstados() {
        assertEquals(EN_REVISION, NombreEstado.desdeEtiqueta("En revisión"));
        assertThrows(IllegalStateException.class, () -> NombreEstado.desdeEtiqueta("Priorizado"));
    }
}
