package com.ecomarket.iniciosesion.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Entrada para cambiar el correo: exige la contrasena del cliente y el nuevo correo.
 */
public record CambiarCorreoRequestDTO(

        @NotNull(message = "El clienteId es obligatorio")
        Long clienteId,

        @NotBlank(message = "La contrasena es obligatoria")
        String contrasena,

        @NotBlank(message = "El nuevo correo es obligatorio")
        @Email(message = "El nuevo correo no tiene un formato valido")
        String nuevoCorreo
) {
}
