package com.ecomarket.carritocompra.exception;

public class CarritoVacioException extends RuntimeException {
    public CarritoVacioException(String mensaje) {
        super(mensaje);
    }
}