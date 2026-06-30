package com.ecomarket.carritocompra.dto;

import java.math.BigDecimal;
import java.util.List;

public record CarritoResponseDTO(
        Long id,
        Long clienteId,
        Boolean activo,
        List<ItemCarritoResponseDTO> items,
        BigDecimal total
) {
}