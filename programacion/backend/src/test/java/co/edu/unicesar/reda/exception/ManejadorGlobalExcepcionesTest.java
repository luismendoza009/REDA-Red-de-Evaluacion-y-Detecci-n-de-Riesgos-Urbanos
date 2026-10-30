package co.edu.unicesar.reda.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ManejadorGlobalExcepcionesTest {

    /** Si alguien agrega una categoría y olvida traducirla, el switch no compila; esta prueba documenta el mapa. */
    @ParameterizedTest
    @EnumSource(CategoriaError.class)
    void todaCategoriaTieneUnCodigoHttp(CategoriaError categoria) {
        assertNotNull(ManejadorGlobalExcepciones.estadoHttp(categoria));
    }

    @Test
    void cadaExcepcionPropiaSeTraduceAlCodigoEsperado() {
        assertEquals(HttpStatus.NOT_FOUND, estado(new RecursoNoEncontradoException("Reporte", 1)));
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, estado(new ReglaNegocioException("X", "regla")));
        assertEquals(HttpStatus.CONFLICT, estado(new ConflictoException("X", "conflicto")));
        assertEquals(HttpStatus.UNAUTHORIZED, estado(new CredencialesInvalidasException()));
        assertEquals(HttpStatus.FORBIDDEN, estado(new AccesoDenegadoException("no")));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE,
                ManejadorGlobalExcepciones.estadoHttp(CategoriaError.SERVICIO_NO_DISPONIBLE));
    }

    private static HttpStatus estado(RedaException e) {
        return ManejadorGlobalExcepciones.estadoHttp(e.getCategoria());
    }
}
