package com.ecomarket.carritocompra.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ecomarket.carritocompra.dto.CarritoResponseDTO;
import com.ecomarket.carritocompra.dto.ItemCarritoResponseDTO;
import com.ecomarket.carritocompra.service.CarritoService;

/**
 * TIPO 2 - Cobertura de los endpoints restantes de CarritoController:
 * agregar (camino feliz 200), eliminar item y vaciar carrito.
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
    void agregar_valido_devuelve200() throws Exception {
        CarritoResponseDTO conItem = new CarritoResponseDTO(1L, 1L, true,
                List.of(new ItemCarritoResponseDTO(1L, 5L, "Arroz", new BigDecimal("2590.00"),
                        2, new BigDecimal("5180.00"))),
                new BigDecimal("5180.00"));
        when(carritoService.agregarProducto(anyLong(), anyLong(), anyInt())).thenReturn(conItem);

        mockMvc.perform(post("/api/v1/carritos/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":1,"productoId":5,"cantidad":2}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(5180.00))
                .andExpect(jsonPath("$.items[0].nombreProducto").value("Arroz"));
    }

    @Test
    void eliminarItem_devuelve200() throws Exception {
        when(carritoService.eliminarItem(1L, 5L)).thenReturn(carritoVacio());

        mockMvc.perform(delete("/api/v1/carritos/1/items/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void vaciar_devuelve200() throws Exception {
        when(carritoService.vaciarCarrito(1L)).thenReturn(carritoVacio());

        mockMvc.perform(delete("/api/v1/carritos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }
}
