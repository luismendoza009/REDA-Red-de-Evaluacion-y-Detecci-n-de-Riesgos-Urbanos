package co.edu.unicesar.reda.exception;

/** Se superó el número de intentos permitidos (protección contra fuerza bruta). */
public class DemasiadosIntentosException extends RedaException {

    private final Long segundosRestantes;

    public DemasiadosIntentosException(long minutosDeEspera) {
        super(CategoriaError.DEMASIADOS_INTENTOS, "DEMASIADOS_INTENTOS",
                "Demasiados intentos. Espere " + minutosDeEspera + " minutos e intente de nuevo");
        this.segundosRestantes = null;
    }

    private DemasiadosIntentosException(String codigo, String mensaje, Long segundosRestantes) {
        super(CategoriaError.DEMASIADOS_INTENTOS, codigo, mensaje);
        this.segundosRestantes = segundosRestantes;
    }

    public static DemasiadosIntentosException temporal(long segundosRestantes) {
        return new DemasiadosIntentosException("DEMASIADOS_INTENTOS",
                "Demasiados intentos. Espere antes de intentar nuevamente", segundosRestantes);
    }

    public static DemasiadosIntentosException permanente() {
        return new DemasiadosIntentosException("CUENTA_BLOQUEADA",
                "Acceso bloqueado por varios intentos fallidos. Comunícate con el administrador para habilitar tu acceso.",
                null);
    }

    public Long getSegundosRestantes() {
        return segundosRestantes;
    }
}
