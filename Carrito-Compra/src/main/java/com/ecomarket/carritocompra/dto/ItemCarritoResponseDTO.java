package com.ecomarket.carritocompra.dto;

import java.math.BigDecimal;

public record ItemCarritoResponseDTO(
        Long id,
        Long productoId,
        String nombreProducto,
        BigDecimal precioUnitario,
        Integer cantidad,
        BigDecimal subtotal
) {
}