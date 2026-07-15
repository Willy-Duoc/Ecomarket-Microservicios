package com.ecomarket.iniciosesion.config;

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

import com.ecomarket.iniciosesion.model.Cliente;
import com.ecomarket.iniciosesion.repository.ClienteRepository;

/**
 * TIPO 1 - Prueba del seed: siembra exactamente 5 clientes si la tabla esta vacia
 * y no duplica si ya hay datos (idempotencia).
 */
@ExtendWith(MockitoExtension.class)
class CargaClientesInicialesTest {

    @Mock
    private ClienteRepository clienteRepository;
    @InjectMocks
    private CargaClientesIniciales cargaClientesIniciales;

    @Test
    void run_conTablaVacia_siembra5Clientes() {
        when(clienteRepository.count()).thenReturn(0L, 5L);

        cargaClientesIniciales.run();

        verify(clienteRepository, times(5)).save(any(Cliente.class));
    }

    @Test
    void run_conDatosExistentes_noSiembra() {
        when(clienteRepository.count()).thenReturn(5L);

        cargaClientesIniciales.run();

        verify(clienteRepository, never()).save(any(Cliente.class));
    }
}
