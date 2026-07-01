package com.ecomarket.carritocompra.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

/**
 * Cobertura completa de las entidades Carrito y Pedido:
 * - rama de alCrear() cuando los valores YA vienen definidos (no se sobreescriben)
 * - constructor @AllArgsConstructor y getters/setters generados por Lombok.
 */
class CarritoEntidadesCoberturaTest {

    @Test
    void carrito_alCrear_conValoresPresentes_noLosSobreescribe() {
        LocalDateTime fecha = LocalDateTime.of(2020, 1, 1, 10, 0);
        Carrito carrito = Carrito.builder()
                .activo(false)
                .fechaCreacion(fecha)
                .fechaUltimaModificacion(fecha)
                .build();

        carrito.alCrear();

        assertThat(carrito.getActivo()).isFalse();
        assertThat(carrito.getFechaCreacion()).isEqualTo(fecha);
        assertThat(carrito.getFechaUltimaModificacion()).isEqualTo(fecha);
    }

    @Test
    void pedido_alCrear_conValoresPresentes_noLosSobreescribe() {
        LocalDateTime fecha = LocalDateTime.of(2020, 1, 1, 10, 0);
        Pedido pedido = Pedido.builder()
                .estado(EstadoPedido.CANCELADO)
                .fechaCreacion(fecha)
                .build();

        pedido.alCrear();

        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.CANCELADO);
        assertThat(pedido.getFechaCreacion()).isEqualTo(fecha);
    }

    @Test
    void carrito_constructorCompletoYSetters() {
        LocalDateTime ahora = LocalDateTime.now();
        Carrito carrito = new Carrito(1L, 2L, true, ahora, ahora, new ArrayList<>());

        carrito.setClienteId(99L);
        carrito.setActivo(false);

        assertThat(carrito.getId()).isEqualTo(1L);
        assertThat(carrito.getClienteId()).isEqualTo(99L);
        assertThat(carrito.getActivo()).isFalse();
        assertThat(carrito.getItems()).isEmpty();
    }

    @Test
    void pedido_constructorCompletoYSetters() {
        LocalDateTime ahora = LocalDateTime.now();
        Pedido pedido = new Pedido(1L, 2L, new BigDecimal("100.00"),
                EstadoPedido.CONFIRMADO, ahora, new ArrayList<>());

        pedido.setTotal(new BigDecimal("200.00"));
        pedido.setEstado(EstadoPedido.CANCELADO);

        assertThat(pedido.getId()).isEqualTo(1L);
        assertThat(pedido.getClienteId()).isEqualTo(2L);
        assertThat(pedido.getTotal()).isEqualByComparingTo("200.00");
        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.CANCELADO);
    }

    @Test
    void itemCarrito_y_itemPedido_settersYGetters() {
        ItemCarrito ic = new ItemCarrito();
        ic.setProductoId(5L);
        ic.setNombreProducto("Arroz");
        ic.setPrecioUnitario(new BigDecimal("2590.00"));
        ic.setCantidad(2);
        assertThat(ic.calcularSubtotal()).isEqualByComparingTo("5180.00");

        ItemPedido ip = new ItemPedido();
        ip.setProductoId(5L);
        ip.setNombreProducto("Arroz");
        ip.setPrecioUnitario(new BigDecimal("2590.00"));
        ip.setCantidad(3);
        assertThat(ip.calcularSubtotal()).isEqualByComparingTo("7770.00");
    }
}
