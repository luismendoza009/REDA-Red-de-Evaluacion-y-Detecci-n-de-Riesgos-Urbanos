package co.edu.unicesar.reda.exception;

/** Se superó el número de intentos permitidos (protección contra fuerza bruta). */
public class DemasiadosIntentosException extends RedaException {

    public DemasiadosIntentosException(long minutosDeEspera) {
        super(CategoriaError.DEMASIADOS_INTENTOS, "DEMASIADOS_INTENTOS",
                "Demasiados intentos. Espere " + minutosDeEspera + " minutos e intente de nuevo");
    }
}
