package co.edu.unicesar.reda.model;

/**
 * Situación pública de un reporte, en el lenguaje del ciudadano. La calcula la vista v_mapa_publico
 * (migración 009) a partir del estado del reporte, del caso y de las remisiones.
 */
public enum SituacionCaso {
    /** Un abogado aceptó el caso y sigue en curso. */
    CON_ABOGADO,
    /** En revisión, validado o remitido sin respuesta: el proceso sigue. */
    ABIERTO,
    /** Caso cerrado o rechazado por los abogados, o falla solucionada. */
    CERRADO
}
