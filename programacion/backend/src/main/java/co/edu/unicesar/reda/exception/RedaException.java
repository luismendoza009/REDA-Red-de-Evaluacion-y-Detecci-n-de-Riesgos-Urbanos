package co.edu.unicesar.reda.exception;

/**
 * Raíz de las excepciones propias de REDA.
 * Son no verificadas (RuntimeException) para no ensuciar las firmas de cada capa, y llevan:
 *  - un código estable que el frontend puede usar sin depender del texto del mensaje;
 *  - una categoría ({@link CategoriaError}) que decide cómo se responde.
 * Ninguna conoce HTTP: así el dominio no depende de la web.
 */
public abstract class RedaException extends RuntimeException {

    private final CategoriaError categoria;
    private final String codigo;

    protected RedaException(CategoriaError categoria, String codigo, String mensaje) {
        super(mensaje);
        this.categoria = categoria;
        this.codigo = codigo;
    }

    public CategoriaError getCategoria() {
        return categoria;
    }

    public String getCodigo() {
        return codigo;
    }
}
