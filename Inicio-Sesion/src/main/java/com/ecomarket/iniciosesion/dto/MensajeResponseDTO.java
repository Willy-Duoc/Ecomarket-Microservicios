package com.ecomarket.iniciosesion.dto;

/** Respuesta simple con un mensaje (logout, cambios de contrasena/correo). */
public record MensajeResponseDTO(String mensaje) {

    public static MensajeResponseDTO de(String mensaje) {
        return new MensajeResponseDTO(mensaje);
    }
}
