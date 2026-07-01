package com.ecomarket.catalogoinventario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
 * TIPO 1 - Cobertura de los metodos restantes de ProductoService:
 * listarTodos, obtenerPorId (ok), buscarPorNombre, listarPorCategoria y actualizar.
 */
@ExtendWith(MockitoExtension.class)
class ProductoServiceCoberturaTest {

    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private ProductoMapper productoMapper;
    @InjectMocks
    private ProductoService productoService;

    private Producto entidad(Long id, String sku) {
        return Producto.builder().id(id).sku(sku).nombre("n").build();
    }

    private ProductoResponseDTO responseDummy() {
        return new ProductoResponseDTO(1L, "ALI-01", "n", "d",
                CategoriaProducto.ALIMENTOS_ORGANICOS, new BigDecimal("1000.00"),
                10, null, null, null);
    }

    private ProductoRequestDTO request(String sku) {
        return new ProductoRequestDTO(sku, "n", "d",
                CategoriaProducto.ALIMENTOS_ORGANICOS, new BigDecimal("1000.00"), 10, null, null);
    }

    @Test
    void listarTodos_devuelveTodosMapeados() {
        when(productoRepository.findAll()).thenReturn(List.of(entidad(1L, "ALI-01"), entidad(2L, "ALI-02")));
        when(productoMapper.aResponse(any())).thenReturn(responseDummy());

        assertThat(productoService.listarTodos()).hasSize(2);
    }

    @Test
    void obtenerPorId_existente_devuelveDTO() {
        Producto p = entidad(1L, "ALI-01");
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productoMapper.aResponse(p)).thenReturn(responseDummy());

        assertThat(productoService.obtenerPorId(1L).sku()).isEqualTo("ALI-01");
    }

    @Test
    void buscarPorNombre_devuelveCoincidencias() {
        when(productoRepository.findByNombreContainingIgnoreCase("qui"))
                .thenReturn(List.of(entidad(1L, "ALI-01")));
        when(productoMapper.aResponse(any())).thenReturn(responseDummy());

        assertThat(productoService.buscarPorNombre("qui")).hasSize(1);
    }

    @Test
    void listarPorCategoria_devuelveDeEsaCategoria() {
        when(productoRepository.findByCategoria(CategoriaProducto.MASCOTAS))
                .thenReturn(List.of(entidad(3L, "MAS-01")));
        when(productoMapper.aResponse(any())).thenReturn(responseDummy());

        assertThat(productoService.listarPorCategoria(CategoriaProducto.MASCOTAS)).hasSize(1);
    }

    @Test
    void actualizar_sinCambioDeSku_guardaYDevuelve() {
        Producto existente = entidad(1L, "ALI-01");
        ProductoRequestDTO dto = request("ALI-01"); // mismo sku

        when(productoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(productoRepository.save(existente)).thenReturn(existente);
        when(productoMapper.aResponse(existente)).thenReturn(responseDummy());

        productoService.actualizar(1L, dto);

        verify(productoMapper).copiarSobre(existente, dto);
        verify(productoRepository).save(existente);
    }

    @Test
    void actualizar_cambioSkuHaciaUnoExistente_lanza409() {
        Producto existente = entidad(1L, "OLD-99");
        ProductoRequestDTO dto = request("ALI-01"); // sku distinto y ya usado

        when(productoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(productoRepository.existsBySku("ALI-01")).thenReturn(true);

        assertThatThrownBy(() -> productoService.actualizar(1L, dto))
                .isInstanceOf(SkuDuplicadoException.class);

        verify(productoRepository, never()).save(any());
    }

    @Test
    void actualizar_inexistente_lanza404() {
        when(productoRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.actualizar(9L, request("ALI-01")))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void actualizar_cambioSkuHaciaUnoLibre_guarda() {
        Producto existente = entidad(1L, "OLD-99");
        ProductoRequestDTO dto = request("NEW-01"); // sku distinto y NO usado

        when(productoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(productoRepository.existsBySku("NEW-01")).thenReturn(false);
        when(productoRepository.save(existente)).thenReturn(existente);
        when(productoMapper.aResponse(existente)).thenReturn(responseDummy());

        productoService.actualizar(1L, dto);

        verify(productoMapper).copiarSobre(existente, dto);
        verify(productoRepository).save(existente);
    }

    @Test
    void eliminar_existente_borraLaEntidad() {
        Producto existente = entidad(1L, "ALI-01");
        when(productoRepository.findById(1L)).thenReturn(Optional.of(existente));

        productoService.eliminar(1L);

        verify(productoRepository).delete(existente);
    }
}
