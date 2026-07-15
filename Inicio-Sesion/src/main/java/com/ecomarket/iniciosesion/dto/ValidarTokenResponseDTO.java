package com.ecomarket.iniciosesion.dto;

/** Salida de la validacion: si el token es valido y a que cliente pertenece. */
public record ValidarTokenResponseDTO(
        boolean valido,
        Long clienteId,
        String correo
) {
    public static ValidarTokenResponseDTO invalido() {
        return new ValidarTokenResponseDTO(false, null, null);
    }
}
