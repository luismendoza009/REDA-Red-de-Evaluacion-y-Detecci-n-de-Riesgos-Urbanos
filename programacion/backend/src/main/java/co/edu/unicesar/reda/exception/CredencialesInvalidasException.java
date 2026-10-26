package co.edu.unicesar.reda.exception;

/**
 * Correo o contraseña incorrectos.
 * El mensaje es el mismo en los dos casos a propósito: no revela si el correo existe.
 */
public class CredencialesInvalidasException extends RedaException {

    public CredencialesInvalidasException() {
        super(CategoriaError.NO_AUTENTICADO, "CREDENCIALES_INVALIDAS", "Correo o contraseña incorrectos");
    }
}
