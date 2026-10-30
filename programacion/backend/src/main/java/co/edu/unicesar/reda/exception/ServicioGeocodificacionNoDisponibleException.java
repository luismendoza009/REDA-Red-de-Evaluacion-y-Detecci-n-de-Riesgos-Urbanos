package co.edu.unicesar.reda.exception;

public class ServicioGeocodificacionNoDisponibleException extends RedaException {

    public ServicioGeocodificacionNoDisponibleException() {
        super(CategoriaError.SERVICIO_NO_DISPONIBLE, "BUSQUEDA_DIRECCION_NO_DISPONIBLE",
                "La búsqueda de direcciones no está disponible en este momento. Puedes marcar el punto en el mapa o usar el GPS.");
    }
}
