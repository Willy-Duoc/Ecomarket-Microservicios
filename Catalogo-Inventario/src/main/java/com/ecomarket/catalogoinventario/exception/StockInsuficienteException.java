package com.ecomarket.catalogoinventario.exception;

public class StockInsuficienteException extends RuntimeException {
    
    public StockInsuficienteException(String mensaje) {
        super(mensaje);
    }
}
