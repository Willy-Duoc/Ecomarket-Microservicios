package com.ecomarket.carritocompra.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException ex, WebRequest req) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler({StockInsuficienteException.class, CarritoVacioException.class})
    public ResponseEntity<ErrorResponse> conflicto(RuntimeException ex, WebRequest req) {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(CatalogoNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> catalogoCaido(CatalogoNoDisponibleException ex, WebRequest req) {
        return construir(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex, WebRequest req) {
        Map<String, String> errores = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errores.put(fe.getField(), fe.getDefaultMessage());
        }
        ErrorResponse cuerpo = ErrorResponse.deValidacion(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Hay errores de validación en la petición",
                path(req), errores);
        return ResponseEntity.badRequest().body(cuerpo);
    }

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
