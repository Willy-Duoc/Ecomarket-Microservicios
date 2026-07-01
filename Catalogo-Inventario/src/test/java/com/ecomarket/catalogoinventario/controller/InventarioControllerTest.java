package com.ecomarket.catalogoinventario.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ecomarket.catalogoinventario.exception.RecursoNoEncontradoException;
import com.ecomarket.catalogoinventario.exception.StockInsuficienteException;
import com.ecomarket.catalogoinventario.service.InventarioService;

/**
 * TIPO 2 - Prueba del InventarioController (@WebMvcTest + MockMvc, sin BD).
 * Verifica rutas, codigos HTTP y JSON de las operaciones de stock.
 */
@WebMvcTest(InventarioController.class)
class InventarioControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private InventarioService inventarioService;

    @Test
    void verificarDisponibilidad_devuelve200yBooleano() throws Exception {
        when(inventarioService.verificarDisponibilidad(1L, 2)).thenReturn(true);

        mockMvc.perform(get("/api/inventario/1/disponibilidad").param("cantidad", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponible").value(true));
    }

    @Test
    void reservar_devuelve200yStockRestante() throws Exception {
        when(inventarioService.reservar(1L, 2)).thenReturn(98);

        mockMvc.perform(post("/api/inventario/1/reservar").param("cantidad", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockRestante").value(98));
    }

    @Test
    void reservar_sinStock_devuelve409() throws Exception {
        when(inventarioService.reservar(1L, 5))
                .thenThrow(new StockInsuficienteException("Stock insuficiente"));

        mockMvc.perform(post("/api/inventario/1/reservar").param("cantidad", "5"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void liberar_devuelve200() throws Exception {
        when(inventarioService.liberar(1L, 2)).thenReturn(102);

        mockMvc.perform(post("/api/inventario/1/liberar").param("cantidad", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockRestante").value(102));
    }

    @Test
    void confirmar_devuelve200() throws Exception {
        when(inventarioService.confirmar(1L, 2)).thenReturn(true);

        mockMvc.perform(post("/api/inventario/1/confirmar").param("cantidad", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmado").value(true));
    }

    @Test
    void ajustarStock_devuelve200() throws Exception {
        when(inventarioService.ajustarStock(1L, 50)).thenReturn(50);

        mockMvc.perform(put("/api/inventario/1/stock").param("nuevaCantidad", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockActual").value(50));
    }

    @Test
    void reservar_productoInexistente_devuelve404() throws Exception {
        when(inventarioService.reservar(99L, 1))
                .thenThrow(new RecursoNoEncontradoException("Producto no encontrado con id: 99"));

        mockMvc.perform(post("/api/inventario/99/reservar").param("cantidad", "1"))
                .andExpect(status().isNotFound());
    }
}
