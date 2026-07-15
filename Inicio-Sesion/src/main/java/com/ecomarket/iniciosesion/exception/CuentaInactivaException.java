package com.ecomarket.iniciosesion.exception;

/** La cuenta del cliente esta INACTIVA: no puede iniciar sesion. HTTP 403 Forbidden. */
public class CuentaInactivaException extends RuntimeException {
    public CuentaInactivaException(String mensaje) {
        super(mensaje);
    }
}
