package co.edu.unicesar.reda.exception;

/**
 * Correo o contraseña incorrectos (se traduce a 401).
 * El mensaje es el mismo en los dos casos a propósito: no revela si el correo existe.
 */
public class CredencialesInvalidasException extends RedaException {

    public CredencialesInvalidasException() {
        super("CREDENCIALES_INVALIDAS", "Correo o contraseña incorrectos");
    }
}
