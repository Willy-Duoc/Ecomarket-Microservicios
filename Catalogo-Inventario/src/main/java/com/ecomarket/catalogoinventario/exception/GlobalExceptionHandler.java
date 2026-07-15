package com.ecomarket.catalogoinventario.exception;

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
    
    /** 404 — recurso inexistente. */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> manejarNoEncontrado(
            RecursoNoEncontradoException ex, WebRequest request) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /** 409 — stock insuficiente. */
    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<ErrorResponse> manejarStock(
            StockInsuficienteException ex, WebRequest request) {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /** 409 — SKU duplicado. */
    @ExceptionHandler(SkuDuplicadoException.class)
    public ResponseEntity<ErrorResponse> manejarSkuDuplicado(
            SkuDuplicadoException ex, WebRequest request) {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /**
     * 400 — fallan las validaciones de {@code @Valid} sobre un DTO.
     * Devuelve un mapa campo→mensaje para que el cliente sepa qué corregir.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidacion(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errores = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errores.put(fe.getField(), fe.getDefaultMessage());
        }
        ErrorResponse cuerpo = ErrorResponse.deValidacion(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Hay errores de validación en la petición",
                extraerPath(request),
                errores);
        return ResponseEntity.badRequest().body(cuerpo);
    }

    /** 500 — cualquier otra excepción no controlada. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarGenerico(Exception ex, WebRequest request) {
        return construir(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error inesperado: " + ex.getMessage(), request);
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus status, String mensaje, WebRequest request) {
        ErrorResponse cuerpo = ErrorResponse.de(
                status.value(), status.getReasonPhrase(), mensaje, extraerPath(request));
        return ResponseEntity.status(status).body(cuerpo);
    }

    private String extraerPath(WebRequest request) {
        // "uri=/api/v1/productos/5" → "/api/v1/productos/5"
        return request.getDescription(false).replace("uri=", "");
    }
}
