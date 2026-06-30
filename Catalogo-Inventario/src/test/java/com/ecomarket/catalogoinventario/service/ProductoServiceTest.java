package com.ecomarket.catalogoinventario.service;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecomarket.catalogoinventario.dto.ProductoRequestDTO;
import com.ecomarket.catalogoinventario.dto.ProductoResponseDTO;
import com.ecomarket.catalogoinventario.exception.RecursoNoEncontradoException;
import com.ecomarket.catalogoinventario.exception.SkuDuplicadoException;
import com.ecomarket.catalogoinventario.mapper.ProductoMapper;
import com.ecomarket.catalogoinventario.model.CategoriaProducto;
import com.ecomarket.catalogoinventario.model.Producto;
import com.ecomarket.catalogoinventario.repository.ProductoRepository;

/**
 * TIPO 1 - Prueba unitaria del servicio (JUnit 5 + Mockito, sin base de datos).
 * Prueba la logica de negocio aislada: el repositorio y el mapper son mocks.
 */
@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private ProductoMapper productoMapper;
    @InjectMocks
    private ProductoService productoService;

    private ProductoRequestDTO requestValido() {
        return new ProductoRequestDTO("ALI-01", "Quinoa Organica", "Saco 1kg",
                CategoriaProducto.ALIMENTOS_ORGANICOS, new BigDecimal("3990.00"), 50, null, null);
    }

    @Test
    void crear_conSkuNuevo_guardaYDevuelveDTO() {
        ProductoRequestDTO req = requestValido();
        Producto entidad = Producto.builder().sku(req.sku()).build();
        Producto guardado = Producto.builder().id(1L).sku(req.sku()).build();

        when(productoRepository.existsBySku("ALI-01")).thenReturn(false);
        when(productoMapper.aEntidad(req)).thenReturn(entidad);
        when(productoRepository.save(entidad)).thenReturn(guardado);
        when(productoMapper.aResponse(guardado)).thenReturn(
                new ProductoResponseDTO(1L, "ALI-01", "Quinoa Organica", "Saco 1kg",
                        CategoriaProducto.ALIMENTOS_ORGANICOS, new BigDecimal("3990.00"),
                        50, null, null, null));

        ProductoResponseDTO resultado = productoService.crear(req);

        assertThat(resultado.id()).isEqualTo(1L);
        verify(productoRepository).save(entidad);
    }

    @Test
    void crear_conSkuExistente_lanza409yNoGuarda() {
        when(productoRepository.existsBySku("ALI-01")).thenReturn(true);

        assertThatThrownBy(() -> productoService.crear(requestValido()))
                .isInstanceOf(SkuDuplicadoException.class)
                .hasMessageContaining("ALI-01");

        verify(productoRepository, never()).save(any());
    }

    @Test
    void obtenerPorId_inexistente_lanza404() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.obtenerPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");
    }

    @Test
    void eliminar_inexistente_lanza404() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.eliminar(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(productoRepository, never()).delete(any());
    }
}