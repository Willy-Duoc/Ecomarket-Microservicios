package com.ecomarket.carritocompra.exception;

public class CatalogoNoDisponibleException extends RuntimeException {
    public CatalogoNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}