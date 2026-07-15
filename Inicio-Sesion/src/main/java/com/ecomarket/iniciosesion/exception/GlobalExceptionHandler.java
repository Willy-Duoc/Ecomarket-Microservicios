package com.ecomarket.iniciosesion.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * Manejador centralizado de errores del microservicio Inicio-Sesion.
 *
 * <p>Mapa de excepcion a codigo HTTP:
 * 401 credenciales/token invalidos - 403 cuenta inactiva - 404 no existe -
 * 409 correo duplicado - 400 validacion o contrasenas que no coinciden - 500 resto.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 401 - correo/contrasena incorrectos o token invalido. */
    @ExceptionHandler({CredencialesInvalidasException.class, TokenInvalidoException.class})
    public ResponseEntity<ErrorResponse> noAutorizado(RuntimeException ex, WebRequest req) {
        return construir(HttpStatus.UNAUTHORIZED, ex.getMessage(), req);
    }

    /** 403 - la cuenta existe pero esta inactiva. */
    @ExceptionHandler(CuentaInactivaException.class)
    public ResponseEntity<ErrorResponse> cuentaInactiva(CuentaInactivaException ex, WebRequest req) {
        return construir(HttpStatus.FORBIDDEN, ex.getMessage(), req);
    }

    /** 404 - cliente o sesion inexistente. */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException ex, WebRequest req) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    /** 409 - el nuevo correo ya esta en uso. */
    @ExceptionHandler(CorreoDuplicadoException.class)
    public ResponseEntity<ErrorResponse> correoDuplicado(CorreoDuplicadoException ex, WebRequest req) {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    /** 400 - la nueva contrasena y su repeticion no coinciden. */
    @ExceptionHandler(ContrasenasNoCoincidenException.class)
    public ResponseEntity<ErrorResponse> contrasenasNoCoinciden(
            ContrasenasNoCoincidenException ex, WebRequest req) {
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    /** 400 - fallan las validaciones @Valid de los DTOs. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex, WebRequest req) {
        Map<String, String> errores = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errores.put(fe.getField(), fe.getDefaultMessage());
        }
        ErrorResponse cuerpo = ErrorResponse.deValidacion(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Hay errores de validacion en la peticion",
                path(req), errores);
        return ResponseEntity.badRequest().body(cuerpo);
    }

    /** 500 - cualquier error no controlado. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> generico(Exception ex, WebRequest req) {
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Error inesperado: " + ex.getMessage(), req);
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus status, String mensaje, WebRequest req) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.de(status.value(), status.getReasonPhrase(), mensaje, path(req)));
    }

    private String path(WebRequest req) {
        return req.getDescription(false).replace("uri=", "");
    }
}
