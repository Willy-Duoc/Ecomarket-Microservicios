package com.ecomarket.iniciosesion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Entrada para cambiar la contrasena: exige la contrasena actual, la nueva
 * y su repeticion para confirmar el cambio.
 */
public record CambiarContrasenaRequestDTO(

        @NotNull(message = "El clienteId es obligatorio")
        Long clienteId,

        @NotBlank(message = "La contrasena actual es obligatoria")
        String contrasenaActual,

        @NotBlank(message = "La nueva contrasena es obligatoria")
        @Size(min = 6, message = "La nueva contrasena debe tener al menos 6 caracteres")
        String nuevaContrasena,

        @NotBlank(message = "Debe repetir la nueva contrasena")
        String repetirContrasena
) {
}
