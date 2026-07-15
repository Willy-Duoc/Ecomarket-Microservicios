package com.ecomarket.iniciosesion.dto;

/** Salida del login: token JWT y datos basicos del cliente autenticado. */
public record LoginResponseDTO(
        String token,
        Long clienteId,
        String nombre,
        String apellido,
        String correo,
        long expiraEnMs
) {
}
