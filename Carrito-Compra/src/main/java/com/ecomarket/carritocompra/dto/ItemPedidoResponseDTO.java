package com.ecomarket.carritocompra.dto;

import java.math.BigDecimal;

public record ItemPedidoResponseDTO(
        Long id,
        Long productoId,
        String nombreProducto,
        BigDecimal precioUnitario,
        Integer cantidad,
        BigDecimal subtotal
) {
}