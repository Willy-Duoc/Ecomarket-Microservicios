package com.ecomarket.iniciosesion.exception;

/** Recurso inexistente (cliente o sesion). HTTP 404 Not Found. */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
