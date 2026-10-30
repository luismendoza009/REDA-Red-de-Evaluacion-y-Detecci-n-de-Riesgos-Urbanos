package co.edu.unicesar.reda.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Formato ÚNICO de error de toda la API: el frontend siempre recibe la misma estructura.
 * 'codigo' es estable (para programar contra él); 'mensaje' es para mostrar a la persona.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorRespuesta(OffsetDateTime fecha, int estado, String codigo, String mensaje, String ruta,
                             Map<String, String> campos, Long segundosRestantes) {

    public ErrorRespuesta(OffsetDateTime fecha, int estado, String codigo, String mensaje, String ruta,
                          Map<String, String> campos) {
        this(fecha, estado, codigo, mensaje, ruta, campos, null);
    }

    public static ErrorRespuesta de(int estado, String codigo, String mensaje, String ruta) {
        return new ErrorRespuesta(OffsetDateTime.now(), estado, codigo, mensaje, ruta, null, null);
    }
}
