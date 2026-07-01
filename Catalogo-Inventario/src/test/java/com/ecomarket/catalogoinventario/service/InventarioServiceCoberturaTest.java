package com.ecomarket.catalogoinventario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecomarket.catalogoinventario.exception.RecursoNoEncontradoException;
import com.ecomarket.catalogoinventario.exception.StockInsuficienteException;
import com.ecomarket.catalogoinventario.model.EstadoProducto;
import com.ecomarket.catalogoinventario.model.Producto;
import com.ecomarket.catalogoinventario.repository.ProductoRepository;

/**
 * TIPO 1 - Cobertura de los metodos restantes de InventarioService:
 * verificarDisponibilidad, confirmar, ajustarStock y la rama DESCONTINUADO.
 */
@ExtendWith(MockitoExtension.class)
class InventarioServiceCoberturaTest {

    @Mock
    private ProductoRepository productoRepository;
    @InjectMocks
    private InventarioService inventarioService;

    private Producto conStock(int stock, EstadoProducto estado) {
        return Producto.builder().id(1L).sku("ALI-01").stock(stock).estado(estado).build();
    }

    @Test
    void verificarDisponibilidad_true_cuandoHayStock() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(conStock(10, EstadoProducto.DISPONIBLE)));
        assertThat(inventarioService.verificarDisponibilidad(1L, 5)).isTrue();
    }

    @Test
    void verificarDisponibilidad_false_cuandoNoAlcanza() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(conStock(3, EstadoProducto.DISPONIBLE)));
        assertThat(inventarioService.verificarDisponibilidad(1L, 5)).isFalse();
    }

    @Test
    void confirmar_productoExistente_devuelveTrue() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(conStock(10, EstadoProducto.DISPONIBLE)));
        assertThat(inventarioService.confirmar(1L, 2)).isTrue();
    }

    @Test
    void confirmar_productoInexistente_lanza404() {
        when(productoRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> inventarioService.confirmar(9L, 1))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void ajustarStock_valorValido_actualiza() {
        Producto p = conStock(10, EstadoProducto.DISPONIBLE);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productoRepository.save(p)).thenReturn(p);

        assertThat(inventarioService.ajustarStock(1L, 3)).isEqualTo(3);
        assertThat(p.getStock()).isEqualTo(3);
    }

    @Test
    void ajustarStock_negativo_lanza409yNoBusca() {
        assertThatThrownBy(() -> inventarioService.ajustarStock(1L, -1))
                .isInstanceOf(StockInsuficienteException.class);
        verify(productoRepository, never()).findById(1L);
    }

    @Test
    void reservar_productoDescontinuado_mantieneEstado() {
        Producto p = conStock(10, EstadoProducto.DESCONTINUADO);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productoRepository.save(p)).thenReturn(p);

        inventarioService.reservar(1L, 2);

        assertThat(p.getStock()).isEqualTo(8);
        assertThat(p.getEstado()).isEqualTo(EstadoProducto.DESCONTINUADO);
    }
}
