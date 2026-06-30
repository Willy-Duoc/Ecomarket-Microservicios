package com.ecomarket.catalogoinventario.dto;

import java.math.BigDecimal;

import com.ecomarket.catalogoinventario.model.CategoriaProducto;
import com.ecomarket.catalogoinventario.model.EstadoProducto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductoRequestDTO(

        @NotBlank(message = "El SKU es obligatorio")
        @Size(max = 50, message = "El SKU no puede superar 50 caracteres")
        String sku,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
        String nombre,

        @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
        String descripcion,

        @NotNull(message = "La categoría es obligatoria")
        CategoriaProducto categoria,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor que cero")
        BigDecimal precio,

        @NotNull(message = "El stock es obligatorio")
        @PositiveOrZero(message = "El stock no puede ser negativo")
        Integer stock,

        @Size(max = 255, message = "La URL de imagen no puede superar 255 caracteres")
        String imagenUrl,

        /** Opcional: si no se envía, el servicio asigna DISPONIBLE por defecto. */
        EstadoProducto estado
) {
}