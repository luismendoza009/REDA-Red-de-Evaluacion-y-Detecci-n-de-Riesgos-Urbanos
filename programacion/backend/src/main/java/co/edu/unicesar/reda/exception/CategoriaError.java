package co.edu.unicesar.reda.exception;

/**
 * Tipo de falla, independiente de HTTP. Cada excepción de REDA declara su categoría y el
 * manejador global traduce la categoría a un código HTTP en un único lugar.
 * Agregar una excepción nueva NO exige tocar el manejador (principio abierto/cerrado).
 */
public enum CategoriaError {
    NO_ENCONTRADO,
    REGLA_NEGOCIO,
    CONFLICTO,
    NO_AUTENTICADO,
    ACCESO_DENEGADO,
    DEMASIADOS_INTENTOS
}
