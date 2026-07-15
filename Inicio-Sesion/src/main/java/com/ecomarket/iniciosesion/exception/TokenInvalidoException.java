package com.ecomarket.iniciosesion.exception;

/** Token JWT invalido, expirado o de una sesion cerrada. HTTP 401 Unauthorized. */
public class TokenInvalidoException extends RuntimeException {
    public TokenInvalidoException(String mensaje) {
        super(mensaje);
    }
}
