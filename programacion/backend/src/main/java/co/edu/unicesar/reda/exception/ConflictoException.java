package co.edu.unicesar.reda.exception;

/** El dato choca con uno existente, p. ej. un correo ya registrado (se traduce a 409). */
public class ConflictoException extends RedaException {

    public ConflictoException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
