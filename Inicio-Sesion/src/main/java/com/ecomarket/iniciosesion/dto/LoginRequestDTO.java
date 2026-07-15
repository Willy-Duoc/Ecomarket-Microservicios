package com.ecomarket.iniciosesion.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Entrada del login: correo y contrasena del cliente. */
public record LoginRequestDTO(

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato valido")
        String correo,

        @NotBlank(message = "La contrasena es obligatoria")
        String contrasena
) {
}
