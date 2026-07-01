package com.ecomarket.catalogoinventario.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecomarket.catalogoinventario.model.Producto;
import com.ecomarket.catalogoinventario.repository.ProductoRepository;

/**
 * TIPO 1 - Prueba unitaria del seed CargaDatosIniciales (Mockito, sin BD).
 * Verifica que siembra exactamente 100 productos cuando la tabla esta vacia,
 * y que NO siembra nada cuando ya hay datos (idempotencia).
 */
@ExtendWith(MockitoExtension.class)
class CargaDatosInicialesTest {

    @Mock
    private ProductoRepository productoRepository;
    @InjectMocks
    private CargaDatosIniciales cargaDatosIniciales;

    @Test
    void run_cuandoTablaVacia_siembra100Productos() {
        when(productoRepository.count()).thenReturn(0L);

        cargaDatosIniciales.run();

        verify(productoRepository, times(100)).save(any(Producto.class));
    }

    @Test
    void run_cuandoYaHayDatos_noSiembra() {
        when(productoRepository.count()).thenReturn(100L);

        cargaDatosIniciales.run();

        verify(productoRepository, never()).save(any(Producto.class));
    }
}
