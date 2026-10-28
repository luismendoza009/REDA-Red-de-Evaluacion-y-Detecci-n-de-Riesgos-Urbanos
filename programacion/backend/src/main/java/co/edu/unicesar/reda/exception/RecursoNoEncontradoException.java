package co.edu.unicesar.reda.exception;

/** El recurso pedido no existe. */
public class RecursoNoEncontradoException extends RedaException {

    public RecursoNoEncontradoException(String recurso, Object id) {
        super(CategoriaError.NO_ENCONTRADO, "NO_ENCONTRADO", recurso + " con id " + id + " no existe");
    }
}
