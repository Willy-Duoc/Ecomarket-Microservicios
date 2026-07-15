package com.ecomarket.iniciosesion.dto;

import jakarta.validation.constraints.NotBlank;

/** Entrada del logout: el token JWT de la sesion a cerrar. */
public record LogoutRequestDTO(

        @NotBlank(message = "El token es obligatorio")
        String token
) {
}
