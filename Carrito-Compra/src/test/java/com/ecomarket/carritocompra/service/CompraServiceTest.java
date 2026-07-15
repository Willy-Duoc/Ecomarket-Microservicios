package com.ecomarket.carritocompra.service;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecomarket.carritocompra.client.CatalogoClient;
import com.ecomarket.carritocompra.dto.PedidoResponseDTO;
import com.ecomarket.carritocompra.exception.CarritoVacioException;
import com.ecomarket.carritocompra.mapper.CarritoMapper;
import com.ecomarket.carritocompra.model.Carrito;
import com.ecomarket.carritocompra.model.EstadoPedido;
import com.ecomarket.carritocompra.model.ItemCarrito;
import com.ecomarket.carritocompra.model.Pedido;
import com.ecomarket.carritocompra.repository.CarritoRepository;
import com.ecomarket.carritocompra.repository.PedidoRepository;

/**
 * TIPO 1 - Prueba unitaria del servicio de compra (Mockito, sin BD).
 * Verifica la generacion del pedido y el rechazo de carrito vacio.
 */
@ExtendWith(MockitoExtension.class)
class CompraServiceTest {

    @Mock private CarritoRepository carritoRepository;
    @Mock private PedidoRepository pedidoRepository;
    @Mock private CarritoService carritoService;
    @Mock private CatalogoClient catalogoClient;

    private final CarritoMapper carritoMapper = new CarritoMapper();
    private CompraService compraService;

    @BeforeEach
    void setUp() {
        compraService = new CompraService(carritoRepository, pedidoRepository,
                carritoService, catalogoClient, carritoMapper);
    }

    @Test
    void confirmarCompra_carritoVacio_lanza409() {
        Carrito vacio = Carrito.builder().id(1L).clienteId(1L).activo(true).build();
        when(carritoService.obtenerOActivar(1L)).thenReturn(vacio);

        assertThatThrownBy(() -> compraService.confirmarCompra(1L))
                .isInstanceOf(CarritoVacioException.class);

        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void confirmarCompra_conItems_generaPedidoConfirmado() {
        Carrito carrito = Carrito.builder().id(1L).clienteId(1L).activo(true).build();
        carrito.getItems().add(ItemCarrito.builder().id(1L).carrito(carrito).productoId(10L)
                .nombreProducto("Quinoa").precioUnitario(new BigDecimal("3990.00")).cantidad(2).build());

        when(carritoService.obtenerOActivar(1L)).thenReturn(carrito);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            p.setId(99L);
            return p;
        });

        PedidoResponseDTO resp = compraService.confirmarCompra(1L);

        // Regla de negocio: el producto comprado se elimina del catalogo
        verify(catalogoClient).eliminarProducto(10L);
        assertThat(resp.estado()).isEqualTo(EstadoPedido.CONFIRMADO);
        assertThat(resp.total()).isEqualByComparingTo("7980.00");
        assertThat(carrito.getActivo()).isFalse();
    }
}