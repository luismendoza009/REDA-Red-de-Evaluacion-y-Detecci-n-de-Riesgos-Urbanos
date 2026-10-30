package co.edu.unicesar.reda.exception;

public class LimiteGeocodificacionException extends RedaException {

    public LimiteGeocodificacionException() {
        super(CategoriaError.DEMASIADOS_INTENTOS, "LIMITE_BUSQUEDA_DIRECCION",
                "Espera un segundo antes de realizar otra búsqueda de dirección.");
    }
}
