package com.ecomarket.iniciosesion.exception;

/** El correo nuevo ya esta en uso por otro cliente. HTTP 409 Conflict. */
public class CorreoDuplicadoException extends RuntimeException {
    public CorreoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
