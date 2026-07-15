package com.ecomarket.iniciosesion.exception;

import java.time.LocalDateTime;
import java.util.Map;

/** Cuerpo de error estandar (mismo formato que los demas microservicios). */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String mensaje,
        String path,
        Map<String, String> detallesValidacion
) {
    public static ErrorResponse de(int status, String error, String mensaje, String path) {
        return new ErrorResponse(LocalDateTime.now(), status, error, mensaje, path, null);
    }

    public static ErrorResponse deValidacion(int status, String error, String mensaje,
                                             String path, Map<String, String> detalles) {
        return new ErrorResponse(LocalDateTime.now(), status, error, mensaje, path, detalles);
    }
}
