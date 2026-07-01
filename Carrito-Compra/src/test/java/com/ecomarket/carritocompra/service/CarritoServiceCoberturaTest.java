package com.ecomarket.carritocompra.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecomarket.carritocompra.client.CatalogoClient;
import com.ecomarket.carritocompra.dto.CarritoResponseDTO;
import com.ecomarket.carritocompra.exception.RecursoNoEncontradoException;
import com.ecomarket.carritocompra.mapper.CarritoMapper;
import com.ecomarket.carritocompra.model.Carrito;
import com.ecomarket.carritocompra.model.ItemCarrito;
import com.ecomarket.carritocompra.repository.CarritoRepository;

/**
 * TIPO 1 - Cobertura de los metodos restantes de CarritoService:
 * obtenerCarrito y eliminarItem (exito y 404).
 */
@ExtendWith(MockitoExtension.class)
class CarritoServiceCoberturaTest {

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

    private Carrito carritoConItem() {
        Carrito carrito = Carrito.builder().id(1L).clienteId(1L).activo(true).build();
        carrito.getItems().add(ItemCarrito.builder().id(7L).carrito(carrito).productoId(10L)
                .nombreProducto("Quinoa").precioUnitario(new BigDecimal("3990.00")).cantidad(2).build());
        return carrito;
    }

    @Test
    void obtenerCarrito_existente_devuelveDTO() {
        Carrito carrito = carritoConItem();
        when(carritoRepository.findByClienteIdAndActivoTrue(1L)).thenReturn(Optional.of(carrito));

        CarritoResponseDTO resp = carritoService.obtenerCarrito(1L);

        assertThat(resp.clienteId()).isEqualTo(1L);
        assertThat(resp.items()).hasSize(1);
    }

    @Test
    void eliminarItem_existente_liberaStockyLoQuita() {
        Carrito carrito = carritoConItem();
        when(carritoRepository.findByClienteIdAndActivoTrue(1L)).thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        CarritoResponseDTO resp = carritoService.eliminarItem(1L, 7L);

        verify(catalogoClient).liberar(10L, 2);
        assertThat(resp.items()).isEmpty();
    }

    @Test
    void eliminarItem_inexistente_lanza404yNoLibera() {
        Carrito carrito = carritoConItem();
        when(carritoRepository.findByClienteIdAndActivoTrue(1L)).thenReturn(Optional.of(carrito));

        assertThatThrownBy(() -> carritoService.eliminarItem(1L, 999L))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(catalogoClient, never()).liberar(anyLong(), anyInt());
    }
}
