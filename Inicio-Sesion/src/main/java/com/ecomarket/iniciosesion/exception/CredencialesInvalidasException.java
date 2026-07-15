package com.ecomarket.iniciosesion.exception;

/** Correo o contrasena incorrectos. Se traduce a HTTP 401 Unauthorized. */
public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
