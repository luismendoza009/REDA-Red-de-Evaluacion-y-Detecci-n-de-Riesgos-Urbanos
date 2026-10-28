package co.edu.unicesar.reda.exception;

/** El usuario está autenticado pero no puede ver o modificar ese recurso. */
public class AccesoDenegadoException extends RedaException {

    public AccesoDenegadoException(String mensaje) {
        super(CategoriaError.ACCESO_DENEGADO, "ACCESO_DENEGADO", mensaje);
    }
}
