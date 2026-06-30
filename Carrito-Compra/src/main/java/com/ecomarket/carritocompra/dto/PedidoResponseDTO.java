package com.ecomarket.carritocompra.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.ecomarket.carritocompra.model.EstadoPedido;

public record PedidoResponseDTO(
        Long id,
        Long clienteId,
        BigDecimal total,
        EstadoPedido estado,
        LocalDateTime fechaCreacion,
        List<ItemPedidoResponseDTO> items
) {
}