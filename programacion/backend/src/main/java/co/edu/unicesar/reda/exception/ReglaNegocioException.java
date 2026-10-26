package co.edu.unicesar.reda.exception;

/** Se intentó algo que el dominio no permite, aunque los datos tengan formato válido (se traduce a 422). */
public class ReglaNegocioException extends RedaException {

    public ReglaNegocioException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
