package com.ecomarket.carritocompra.service;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecomarket.carritocompra.client.CatalogoClient;
import com.ecomarket.carritocompra.client.dto.ProductoCatalogoDTO;
import com.ecomarket.carritocompra.dto.CarritoResponseDTO;
import com.ecomarket.carritocompra.exception.StockInsuficienteException;
import com.ecomarket.carritocompra.mapper.CarritoMapper;
import com.ecomarket.carritocompra.model.Carrito;
import com.ecomarket.carritocompra.model.ItemCarrito;
import com.ecomarket.carritocompra.repository.CarritoRepository;

/**
 * TIPO 1 - Prueba unitaria del servicio del carrito (Mockito, sin BD).
 * El cliente del catalogo (CatalogoClient) es un mock: probamos la coordinacion
 * snapshot + reserva sin levantar el catalogo real.
 */
@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    @Mock
    private CarritoRepository carritoRepository;
    @Mock
    private CatalogoClient catalogoClient;

    private final CarritoMapper carritoMapper = new CarritoMapper();
    private CarritoService carritoService;

    @BeforeEach
    void setUp() {
        carritoService = new CarritoService(carritoRepository, catalogoClient, carritoMapper);
    }

    private ProductoCatalogoDTO producto() {
        return new ProductoCatalogoDTO(10L, "Quinoa Organica", new BigDecimal("3990.00"), 100, "DISPONIBLE");
    }

    @Test
    void agregarProducto_reservaStockYGuardaSnapshot() {
        when(catalogoClient.obtenerProducto(10L)).thenReturn(producto());
        when(carritoRepository.findByClienteIdAndActivoTrue(1L)).thenReturn(Optional.empty());
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        CarritoResponseDTO resp = carritoService.agregarProducto(1L, 10L, 2);

        verify(catalogoClient).reservar(10L, 2);
        assertThat(resp.items()).hasSize(1);
        assertThat(resp.items().get(0).nombreProducto()).isEqualTo("Quinoa Organica");
        assertThat(resp.total()).isEqualByComparingTo("7980.00");
    }

    @Test
    void agregarProducto_sinStock_propaga409yNoGuarda() {
        when(catalogoClient.obtenerProducto(10L)).thenReturn(producto());
        org.mockito.Mockito.doThrow(new StockInsuficienteException("sin stock"))
                .when(catalogoClient).reservar(eq(10L), anyInt());

        assertThatThrownBy(() -> carritoService.agregarProducto(1L, 10L, 5))
                .isInstanceOf(StockInsuficienteException.class);

        verify(carritoRepository, never()).save(any());
    }

    @Test
    void vaciarCarrito_liberaStockDeCadaItem() {
        Carrito carrito = Carrito.builder().id(1L).clienteId(1L).activo(true).build();
        carrito.getItems().add(ItemCarrito.builder().id(1L).carrito(carrito).productoId(10L)
                .nombreProducto("Quinoa").precioUnitario(new BigDecimal("3990.00")).cantidad(2).build());

        when(carritoRepository.findByClienteIdAndActivoTrue(1L)).thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        CarritoResponseDTO resp = carritoService.vaciarCarrito(1L);

        verify(catalogoClient).liberar(10L, 2);
        assertThat(resp.items()).isEmpty();
    }

    @Test
    void agregarMismoProductoDosVeces_sumaCantidad() {
        when(catalogoClient.obtenerProducto(anyLong())).thenReturn(producto());
        Carrito carrito = Carrito.builder().id(1L).clienteId(1L).activo(true).build();
        when(carritoRepository.findByClienteIdAndActivoTrue(1L)).thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        carritoService.agregarProducto(1L, 10L, 2);
        CarritoResponseDTO resp = carritoService.agregarProducto(1L, 10L, 3);

        assertThat(resp.items()).hasSize(1);
        assertThat(resp.items().get(0).cantidad()).isEqualTo(5);
    }
}