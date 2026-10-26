package co.edu.unicesar.reda.exception;

/**
 * Raíz de las excepciones propias de REDA.
 * Son no verificadas (RuntimeException) para no ensuciar las firmas de cada capa, y llevan un
 * código estable que el frontend puede usar sin depender del texto del mensaje.
 * Ninguna conoce HTTP: la traducción a códigos de estado se hace en un solo lugar
 * (ManejadorGlobalExcepciones), así el dominio no depende de la web.
 */
public abstract class RedaException extends RuntimeException {

    private final String codigo;

    protected RedaException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
