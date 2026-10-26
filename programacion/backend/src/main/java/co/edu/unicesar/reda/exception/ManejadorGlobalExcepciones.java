package co.edu.unicesar.reda.exception;

import co.edu.unicesar.reda.dto.ErrorRespuesta;
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

    @ExceptionHandler(RecursoNoEncontradoException.class)
    ResponseEntity<ErrorRespuesta> noEncontrado(RedaException e, HttpServletRequest p) {
        return responder(HttpStatus.NOT_FOUND, e.getCodigo(), e.getMessage(), p);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    ResponseEntity<ErrorRespuesta> reglaNegocio(RedaException e, HttpServletRequest p) {
        return responder(HttpStatus.UNPROCESSABLE_ENTITY, e.getCodigo(), e.getMessage(), p);
    }

    @ExceptionHandler(ConflictoException.class)
    ResponseEntity<ErrorRespuesta> conflicto(RedaException e, HttpServletRequest p) {
        return responder(HttpStatus.CONFLICT, e.getCodigo(), e.getMessage(), p);
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    ResponseEntity<ErrorRespuesta> credenciales(RedaException e, HttpServletRequest p) {
        return responder(HttpStatus.UNAUTHORIZED, e.getCodigo(), e.getMessage(), p);
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    ResponseEntity<ErrorRespuesta> accesoDenegado(RedaException e, HttpServletRequest p) {
        return responder(HttpStatus.FORBIDDEN, e.getCodigo(), e.getMessage(), p);
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

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
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
