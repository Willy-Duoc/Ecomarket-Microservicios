package com.ecomarket.carritocompra.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import com.ecomarket.carritocompra.exception.CatalogoNoDisponibleException;
import com.ecomarket.carritocompra.service.CarritoService;

/**
 * TIPO 2 - Prueba del controlador del carrito (@WebMvcTest + MockMvc, sin BD).
 * Verifica el contrato HTTP, incluida la traduccion de "catalogo caido" -> 503.
 */
@WebMvcTest(CarritoController.class)
class CarritoControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private CarritoService carritoService;

    @Test
    void obtener_devuelve200ConTotal() throws Exception {
        CarritoResponseDTO dto = new CarritoResponseDTO(1L, 1L, true,
                List.of(new ItemCarritoResponseDTO(1L, 10L, "Quinoa", new BigDecimal("3990.00"),
                        2, new BigDecimal("7980.00"))),
                new BigDecimal("7980.00"));
        when(carritoService.obtenerCarrito(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/carrito/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(7980.00))
                .andExpect(jsonPath("$.items[0].nombreProducto").value("Quinoa"));
    }

    @Test
    void agregar_conCantidadCero_devuelve400() throws Exception {
        String json = """
                {"clienteId":1,"productoId":10,"cantidad":0}
                """;

        mockMvc.perform(post("/api/carrito/items")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detallesValidacion.cantidad").exists());
    }

    @Test
    void agregar_conCatalogoCaido_devuelve503() throws Exception {
        when(carritoService.agregarProducto(anyLong(), anyLong(), anyInt()))
                .thenThrow(new CatalogoNoDisponibleException("Catalogo no disponible"));

        String json = """
                {"clienteId":1,"productoId":10,"cantidad":2}
                """;

        mockMvc.perform(post("/api/carrito/items")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }
}