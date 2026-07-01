package com.ecomarket.carritocompra.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ecomarket.carritocompra.dto.CarritoResponseDTO;
import com.ecomarket.carritocompra.service.CarritoService;

/**
 * TIPO 2 - Cobertura de los endpoints restantes de CarritoController:
 * eliminar item y vaciar carrito.
 */
@WebMvcTest(CarritoController.class)
class CarritoControllerCoberturaTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private CarritoService carritoService;

    private CarritoResponseDTO carritoVacio() {
        return new CarritoResponseDTO(1L, 1L, true, List.of(), BigDecimal.ZERO);
    }

    @Test
    void eliminarItem_devuelve200() throws Exception {
        when(carritoService.eliminarItem(1L, 5L)).thenReturn(carritoVacio());

        mockMvc.perform(delete("/api/carrito/1/items/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void vaciar_devuelve200() throws Exception {
        when(carritoService.vaciarCarrito(1L)).thenReturn(carritoVacio());

        mockMvc.perform(delete("/api/carrito/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }
}
