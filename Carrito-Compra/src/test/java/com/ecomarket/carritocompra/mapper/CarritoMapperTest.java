package com.ecomarket.carritocompra.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.ecomarket.carritocompra.dto.CarritoResponseDTO;
import com.ecomarket.carritocompra.dto.PedidoResponseDTO;
import com.ecomarket.carritocompra.model.Carrito;
import com.ecomarket.carritocompra.model.EstadoPedido;
import com.ecomarket.carritocompra.model.ItemCarrito;
import com.ecomarket.carritocompra.model.ItemPedido;
import com.ecomarket.carritocompra.model.Pedido;

/**
 * TIPO 1 - Prueba unitaria del CarritoMapper (logica pura, sin Spring).
 * Verifica el mapeo a DTO y el calculo de subtotales y total.
 */
class CarritoMapperTest {

    private final CarritoMapper mapper = new CarritoMapper();

    @Test
    void aResponse_calculaSubtotalesyTotal() {
        Carrito carrito = Carrito.builder().id(1L).clienteId(1L).activo(true).build();
        carrito.getItems().add(ItemCarrito.builder().id(1L).carrito(carrito).productoId(10L)
                .nombreProducto("Quinoa").precioUnitario(new BigDecimal("3990.00")).cantidad(2).build());

        CarritoResponseDTO dto = mapper.aResponse(carrito);

        assertThat(dto.items()).hasSize(1);
        assertThat(dto.items().get(0).subtotal()).isEqualByComparingTo("7980.00");
        assertThat(dto.total()).isEqualByComparingTo("7980.00");
    }

    @Test
    void aPedidoResponse_mapeaPedidoConItems() {
        Pedido pedido = Pedido.builder().id(9L).clienteId(1L)
                .total(new BigDecimal("3990.00")).estado(EstadoPedido.CONFIRMADO).build();
        pedido.getItems().add(ItemPedido.builder().id(1L).pedido(pedido).productoId(10L)
                .nombreProducto("Quinoa").precioUnitario(new BigDecimal("3990.00")).cantidad(1).build());

        PedidoResponseDTO dto = mapper.aPedidoResponse(pedido);

        assertThat(dto.id()).isEqualTo(9L);
        assertThat(dto.estado()).isEqualTo(EstadoPedido.CONFIRMADO);
        assertThat(dto.items().get(0).subtotal()).isEqualByComparingTo("3990.00");
    }
}
