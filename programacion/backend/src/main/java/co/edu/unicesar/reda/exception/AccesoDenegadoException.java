package co.edu.unicesar.reda.exception;

/** El usuario está autenticado pero no puede ver o modificar ese recurso (se traduce a 403). */
public class AccesoDenegadoException extends RedaException {

    public AccesoDenegadoException(String mensaje) {
        super("ACCESO_DENEGADO", mensaje);
    }
}
