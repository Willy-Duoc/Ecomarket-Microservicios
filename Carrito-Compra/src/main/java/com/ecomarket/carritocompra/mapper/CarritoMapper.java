package com.ecomarket.carritocompra.mapper;

import org.springframework.stereotype.Component;

import com.ecomarket.carritocompra.dto.CarritoResponseDTO;
import com.ecomarket.carritocompra.dto.ItemCarritoResponseDTO;
import com.ecomarket.carritocompra.dto.ItemPedidoResponseDTO;
import com.ecomarket.carritocompra.dto.PedidoResponseDTO;
import com.ecomarket.carritocompra.model.Carrito;
import com.ecomarket.carritocompra.model.ItemCarrito;
import com.ecomarket.carritocompra.model.ItemPedido;
import com.ecomarket.carritocompra.model.Pedido;

@Component
public class CarritoMapper {
    
    public ItemCarritoResponseDTO aItemResponse(ItemCarrito item) {
        return new ItemCarritoResponseDTO(
                item.getId(),
                item.getProductoId(),
                item.getNombreProducto(),
                item.getPrecioUnitario(),
                item.getCantidad(),
                item.calcularSubtotal());
    }

    public CarritoResponseDTO aResponse(Carrito carrito) {
        return new CarritoResponseDTO(
                carrito.getId(),
                carrito.getClienteId(),
                carrito.getActivo(),
                carrito.getItems().stream().map(this::aItemResponse).toList(),
                carrito.calcularTotal());
    }

    public ItemPedidoResponseDTO aItemPedidoResponse(ItemPedido item) {
        return new ItemPedidoResponseDTO(
                item.getId(),
                item.getProductoId(),
                item.getNombreProducto(),
                item.getPrecioUnitario(),
                item.getCantidad(),
                item.calcularSubtotal());
    }

    public PedidoResponseDTO aPedidoResponse(Pedido pedido) {
        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getClienteId(),
                pedido.getTotal(),
                pedido.getEstado(),
                pedido.getFechaCreacion(),
                pedido.getItems().stream().map(this::aItemPedidoResponse).toList());
    }
}
