package com.ecomarket.carritocompra.client.dto;

import java.math.BigDecimal;

public record ProductoCatalogoDTO(
        Long id,
        String nombre,
        BigDecimal precio,
        Integer stock,
        String estado
) {
}