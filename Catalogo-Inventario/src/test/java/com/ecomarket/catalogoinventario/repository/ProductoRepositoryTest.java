package com.ecomarket.catalogoinventario.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ecomarket.catalogoinventario.model.CategoriaProducto;

/**
 * TIPO 3 - Prueba del repositorio con contexto real + H2.
 * Usa los 100 productos que siembra CargaDatosIniciales al arrancar el contexto
 * para verificar los query methods derivados del nombre.
 */
@SpringBootTest
class ProductoRepositoryTest {

    @Autowired
    private ProductoRepository productoRepository;

    @Test
    void existsBySku_detectaExistenteYNoExistente() {
        assertThat(productoRepository.existsBySku("ALI-01")).isTrue();
        assertThat(productoRepository.existsBySku("NO-EXISTE-999")).isFalse();
    }

    @Test
    void findByNombreContainingIgnoreCase_buscaParcialSinMayusculas() {
        // Hay varios productos "Solar" en la categoria ENERGIA_SOSTENIBLE
        assertThat(productoRepository.findByNombreContainingIgnoreCase("solar")).isNotEmpty();
    }

    @Test
    void findByCategoria_devuelveLosDiezDeCadaCategoria() {
        assertThat(productoRepository.findByCategoria(CategoriaProducto.ALIMENTOS_ORGANICOS)).hasSize(10);
        assertThat(productoRepository.findByCategoria(CategoriaProducto.MASCOTAS)).hasSize(10);
    }
}
