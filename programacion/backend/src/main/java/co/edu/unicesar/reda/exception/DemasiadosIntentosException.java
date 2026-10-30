package co.edu.unicesar.reda.exception;

/** Se superó el número de intentos permitidos (protección contra fuerza bruta). */
public class DemasiadosIntentosException extends RedaException {

    private final Long segundosRestantes;
    private final Integer intentosRestantes;

    public DemasiadosIntentosException(long minutosDeEspera) {
        super(CategoriaError.DEMASIADOS_INTENTOS, "DEMASIADOS_INTENTOS",
                "Demasiados intentos. Espere " + minutosDeEspera + " minutos e intente de nuevo");
        this.segundosRestantes = null;
        this.intentosRestantes = null;
    }

    private DemasiadosIntentosException(String codigo, String mensaje, Long segundosRestantes,
                                        Integer intentosRestantes) {
        super(CategoriaError.DEMASIADOS_INTENTOS, codigo, mensaje);
        this.segundosRestantes = segundosRestantes;
        this.intentosRestantes = intentosRestantes;
    }

    public static DemasiadosIntentosException temporal(long segundosRestantes, int intentosRestantes) {
        return new DemasiadosIntentosException("DEMASIADOS_INTENTOS",
                "Demasiados intentos. Espere antes de intentar nuevamente", segundosRestantes, intentosRestantes);
    }

    public static DemasiadosIntentosException permanente() {
        return new DemasiadosIntentosException("CUENTA_BLOQUEADA",
                "Acceso bloqueado por varios intentos fallidos. Comunícate con el administrador para habilitar tu acceso.",
                null, 0);
    }

    public Long getSegundosRestantes() {
        return segundosRestantes;
    }

    public Integer getIntentosRestantes() {
        return intentosRestantes;
    }
}
