package com.ecomarket.iniciosesion.exception;

/** La nueva contrasena y su repeticion no coinciden. HTTP 400 Bad Request. */
public class ContrasenasNoCoincidenException extends RuntimeException {
    public ContrasenasNoCoincidenException(String mensaje) {
        super(mensaje);
    }
}
