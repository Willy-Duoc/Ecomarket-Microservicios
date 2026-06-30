package com.ecomarket.carritocompra.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AgregarItemRequestDTO(

        @NotNull(message = "El clienteId es obligatorio")
        Long clienteId,

        @NotNull(message = "El productoId es obligatorio")
        Long productoId,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor que cero")
        Integer cantidad
) {
}