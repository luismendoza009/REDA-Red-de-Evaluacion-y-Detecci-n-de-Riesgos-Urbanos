package co.edu.unicesar.reda.exception;

/**
 * Correo o contraseña incorrectos.
 * El mensaje es el mismo en los dos casos a propósito: no revela si el correo existe.
 */
public class CredencialesInvalidasException extends RedaException {

    private final Integer intentosRestantes;

    public CredencialesInvalidasException() {
        this(null);
    }

    public CredencialesInvalidasException(int intentosRestantes) {
        this(Integer.valueOf(intentosRestantes));
    }

    private CredencialesInvalidasException(Integer intentosRestantes) {
        super(CategoriaError.NO_AUTENTICADO, "CREDENCIALES_INVALIDAS", "Correo o contraseña incorrectos");
        this.intentosRestantes = intentosRestantes;
    }

    public Integer getIntentosRestantes() {
        return intentosRestantes;
    }
}
