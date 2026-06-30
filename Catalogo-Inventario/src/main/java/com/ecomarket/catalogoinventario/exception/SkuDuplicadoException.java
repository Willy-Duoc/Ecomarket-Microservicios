package com.ecomarket.catalogoinventario.exception;

public class SkuDuplicadoException extends RuntimeException{
    
    public SkuDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
