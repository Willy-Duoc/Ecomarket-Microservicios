package com.ecomarket.carritocompra.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

/**
 * TIPO 1 - Prueba unitaria de las entidades del carrito (logica de dominio).
 * Vive en el paquete model para poder invocar los hooks package-private alCrear().
 */
class CarritoModelTest {

    @Test
    void itemCarrito_calcularSubtotal_multiplicaPrecioPorCantidad() {
        ItemCarrito item = ItemCarrito.builder()
                .precioUnitario(new BigDecimal("2740.00")).cantidad(3).build();

        assertThat(item.calcularSubtotal()).isEqualByComparingTo("8220.00");
    }

    @Test
    void carrito_calcularTotal_sumaSubtotales() {
        Carrito carrito = Carrito.builder().clienteId(1L).activo(true).build();
        carrito.getItems().add(ItemCarrito.builder().precioUnitario(new BigDecimal("1000.00")).cantidad(2).build());
        carrito.getItems().add(ItemCarrito.builder().precioUnitario(new BigDecimal("500.00")).cantidad(1).build());

        assertThat(carrito.calcularTotal()).isEqualByComparingTo("2500.00");
    }

    @Test
    void carrito_calcularTotal_vacio_devuelveCero() {
        Carrito carrito = Carrito.builder().clienteId(1L).activo(true).build();
        assertThat(carrito.calcularTotal()).isEqualByComparingTo("0");
    }

    @Test
    void itemPedido_calcularSubtotal_multiplica() {
        ItemPedido item = ItemPedido.builder()
                .precioUnitario(new BigDecimal("3990.00")).cantidad(2).build();

        assertThat(item.calcularSubtotal()).isEqualByComparingTo("7980.00");
    }

    @Test
    void carrito_alCrear_asignaValoresPorDefecto() {
        Carrito carrito = new Carrito();

        carrito.alCrear();

        assertThat(carrito.getActivo()).isTrue();
        assertThat(carrito.getFechaCreacion()).isNotNull();
        assertThat(carrito.getFechaUltimaModificacion()).isNotNull();
    }

    @Test
    void pedido_alCrear_asignaEstadoConfirmadoPorDefecto() {
        Pedido pedido = new Pedido();

        pedido.alCrear();

        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.CONFIRMADO);
        assertThat(pedido.getFechaCreacion()).isNotNull();
    }
}
