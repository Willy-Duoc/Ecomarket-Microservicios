package com.ecomarket.catalogoinventario.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ecomarket.catalogoinventario.model.CategoriaProducto;
import com.ecomarket.catalogoinventario.model.EstadoProducto;

public record ProductoResponseDTO(
        Long id,
        String sku,
        String nombre,
        String descripcion,
        CategoriaProducto categoria,
        BigDecimal precio,
        Integer stock,
        String imagenUrl,
        EstadoProducto estado,
        LocalDateTime fechaCreacion
) {
}
