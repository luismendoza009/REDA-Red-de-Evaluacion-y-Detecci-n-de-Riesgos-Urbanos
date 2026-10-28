package co.edu.unicesar.reda.exception;

/** Se intentó algo que el dominio no permite, aunque los datos tengan formato válido. */
public class ReglaNegocioException extends RedaException {

    public ReglaNegocioException(String codigo, String mensaje) {
        super(CategoriaError.REGLA_NEGOCIO, codigo, mensaje);
    }
}
