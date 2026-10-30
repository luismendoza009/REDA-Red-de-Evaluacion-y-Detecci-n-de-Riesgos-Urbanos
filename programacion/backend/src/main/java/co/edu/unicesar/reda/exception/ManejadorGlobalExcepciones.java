package co.edu.unicesar.reda.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ÚNICO lugar donde las excepciones se traducen a respuestas HTTP.
 * Así ningún controlador tiene try/catch y todos los errores salen con el mismo formato.
 * Regla de seguridad: los errores inesperados (500) NUNCA muestran detalles internos al cliente;
 * se registran en el log con un identificador para poder rastrearlos.
 */
@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalExcepciones.class);

    /**
     * TODAS las excepciones propias de REDA pasan por aquí. Cada una trae su categoría, así que
     * crear una excepción nueva no obliga a modificar esta clase (abierta a extensión, cerrada a
     * modificación). Antes de la auditoría SOLID había un método casi idéntico por excepción.
     */
    @ExceptionHandler(RedaException.class)
    ResponseEntity<ErrorRespuesta> propia(RedaException e, HttpServletRequest p) {
        if (e instanceof DemasiadosIntentosException d && d.getSegundosRestantes() != null) {
            HttpStatus estado = estadoHttp(e.getCategoria());
            return ResponseEntity.status(estado).body(new ErrorRespuesta(OffsetDateTime.now(), estado.value(),
                    e.getCodigo(), e.getMessage(), p.getRequestURI(), null, d.getSegundosRestantes()));
        }
        return responder(estadoHttp(e.getCategoria()), e.getCodigo(), e.getMessage(), p);
    }

    static HttpStatus estadoHttp(CategoriaError categoria) {
        return switch (categoria) {
            case NO_ENCONTRADO -> HttpStatus.NOT_FOUND;
            case REGLA_NEGOCIO -> HttpStatus.UNPROCESSABLE_ENTITY;
            case CONFLICTO -> HttpStatus.CONFLICT;
            case NO_AUTENTICADO -> HttpStatus.UNAUTHORIZED;
            case ACCESO_DENEGADO -> HttpStatus.FORBIDDEN;
            case DEMASIADOS_INTENTOS -> HttpStatus.TOO_MANY_REQUESTS;
        };
    }

    /** @PreAuthorize rechazó la petición: el rol no tiene permiso. */
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorRespuesta> rolSinPermiso(AccessDeniedException e, HttpServletRequest p) {
        return responder(HttpStatus.FORBIDDEN, "ROL_SIN_PERMISO", "Su rol no tiene permiso para esta operación", p);
    }

    /** Falló Bean Validation: se devuelve qué campo falló y por qué. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorRespuesta> validacion(MethodArgumentNotValidException e, HttpServletRequest p) {
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> campos.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ErrorRespuesta(OffsetDateTime.now(), 400, "DATOS_INVALIDOS",
                "Hay campos con datos inválidos", p.getRequestURI(), campos));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    ResponseEntity<ErrorRespuesta> formato(Exception e, HttpServletRequest p) {
        return responder(HttpStatus.BAD_REQUEST, "FORMATO_INVALIDO",
                "El cuerpo o los parámetros de la petición no tienen el formato esperado", p);
    }

    /**
     * Última barrera: una restricción de PostgreSQL (CHECK, UNIQUE, FK) rechazó el dato.
     * Se informa el nombre de la restricción (son nombres descriptivos: ck_..., uq_...), nunca el SQL.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErrorRespuesta> integridad(DataIntegrityViolationException e, HttpServletRequest p) {
        String restriccion = e.getCause() instanceof ConstraintViolationException cv ? cv.getConstraintName() : null;
        log.warn("Restricción de la base violada: {}", restriccion);
        return responder(HttpStatus.CONFLICT, "RESTRICCION_BD",
                "La base de datos rechazó el dato" + (restriccion == null ? "" : " (regla " + restriccion + ")"), p);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorRespuesta> inesperado(Exception e, HttpServletRequest p) {
        String id = Long.toString(System.nanoTime(), 36);
        log.error("Error inesperado [{}] en {}", id, p.getRequestURI(), e);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO",
                "Ocurrió un error inesperado. Referencia: " + id, p);
    }

    private static ResponseEntity<ErrorRespuesta> responder(HttpStatus estado, String codigo, String mensaje,
                                                            HttpServletRequest p) {
        return ResponseEntity.status(estado).body(ErrorRespuesta.de(estado.value(), codigo, mensaje, p.getRequestURI()));
    }
}
