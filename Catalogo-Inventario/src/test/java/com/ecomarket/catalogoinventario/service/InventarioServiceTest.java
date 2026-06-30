package com.ecomarket.catalogoinventario.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecomarket.catalogoinventario.exception.StockInsuficienteException;
import com.ecomarket.catalogoinventario.model.EstadoProducto;
import com.ecomarket.catalogoinventario.model.Producto;
import com.ecomarket.catalogoinventario.repository.ProductoRepository;

/**
 * TIPO 1 - Prueba unitaria del servicio de inventario (Mockito, sin BD).
 * Verifica la regla anti-sobreventa: reservar descuenta, liberar suma, sin stock -> 409.
 */
@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private ProductoRepository productoRepository;
    @InjectMocks
    private InventarioService inventarioService;

    private Producto conStock(int stock) {
        return Producto.builder().id(1L).sku("ALI-01").stock(stock)
                .estado(EstadoProducto.DISPONIBLE).build();
    }

    @Test
    void reservar_conStockSuficiente_descuenta() {
        Producto p = conStock(10);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productoRepository.save(p)).thenReturn(p);

        int restante = inventarioService.reservar(1L, 4);

        assertThat(restante).isEqualTo(6);
        verify(productoRepository).save(p);
    }

    @Test
    void reservar_sinStock_lanza409yNoGuarda() {
        Producto p = conStock(3);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> inventarioService.reservar(1L, 5))
                .isInstanceOf(StockInsuficienteException.class);

        assertThat(p.getStock()).isEqualTo(3);
        verify(productoRepository, never()).save(p);
    }

    @Test
    void liberar_devuelveUnidades() {
        Producto p = conStock(6);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productoRepository.save(p)).thenReturn(p);

        assertThat(inventarioService.liberar(1L, 4)).isEqualTo(10);
    }

    @Test
    void reservar_hastaAgotar_marcaAgotado() {
        Producto p = conStock(5);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productoRepository.save(p)).thenReturn(p);

        inventarioService.reservar(1L, 5);

        assertThat(p.getStock()).isZero();
        assertThat(p.getEstado()).isEqualTo(EstadoProducto.AGOTADO);
    }
}