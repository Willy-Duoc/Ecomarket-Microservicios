package com.ecomarket.carritocompra.dto;

import jakarta.validation.constraints.NotNull;

public record ConfirmarCompraRequestDTO(

        @NotNull(message = "El clienteId es obligatorio")
        Long clienteId
) {
}