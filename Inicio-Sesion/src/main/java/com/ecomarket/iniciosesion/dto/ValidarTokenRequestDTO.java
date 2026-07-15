package com.ecomarket.iniciosesion.dto;

import jakarta.validation.constraints.NotBlank;

/** Entrada de la validacion de token (la usan el gateway u otros servicios si lo necesitan). */
public record ValidarTokenRequestDTO(

        @NotBlank(message = "El token es obligatorio")
        String token
) {
}
