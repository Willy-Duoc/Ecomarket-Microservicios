package com.ecomarket.catalogoinventario.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ecomarket.catalogoinventario.dto.ProductoResponseDTO;
import com.ecomarket.catalogoinventario.exception.RecursoNoEncontradoException;
import com.ecomarket.catalogoinventario.model.CategoriaProducto;
import com.ecomarket.catalogoinventario.model.EstadoProducto;
import com.ecomarket.catalogoinventario.service.ProductoService;

/**
 * TIPO 2 - Prueba del controlador (@WebMvcTest + MockMvc, sin BD).
 * Levanta SOLO la capa web; el servicio es un mock (@MockitoBean).
 * Verifica rutas, codigos HTTP y JSON.
 */
@WebMvcTest(ProductoController.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ProductoService productoService;

    @Test
    void obtener_existente_devuelve200yJson() throws Exception {
        ProductoResponseDTO dto = new ProductoResponseDTO(1L, "ALI-01", "Quinoa Organica", "Saco 1kg",
                CategoriaProducto.ALIMENTOS_ORGANICOS, new BigDecimal("3990.00"),
                50, null, EstadoProducto.DISPONIBLE, null);
        when(productoService.obtenerPorId(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/catalogo/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sku").value("ALI-01"));
    }

    @Test
    void obtener_inexistente_devuelve404() throws Exception {
        when(productoService.obtenerPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("Producto no encontrado con id: 99"));

        mockMvc.perform(get("/api/catalogo/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void crear_conCuerpoInvalido_devuelve400() throws Exception {
        String jsonInvalido = """
                {"sku":"","nombre":"","categoria":"ALIMENTOS_ORGANICOS","precio":-5,"stock":-1}
                """;

        mockMvc.perform(post("/api/catalogo")
                        .contentType(MediaType.APPLICATION_JSON).content(jsonInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detallesValidacion").exists());
    }

    @Test
    void crear_valido_devuelve201() throws Exception {
        ProductoResponseDTO creado = new ProductoResponseDTO(5L, "ALI-09", "Azucar de Coco", "Bolsa",
                CategoriaProducto.ALIMENTOS_ORGANICOS, new BigDecimal("2990.00"),
                30, null, EstadoProducto.DISPONIBLE, null);
        when(productoService.crear(any())).thenReturn(creado);

        String json = """
                {"sku":"ALI-09","nombre":"Azucar de Coco","categoria":"ALIMENTOS_ORGANICOS","precio":2990.00,"stock":30}
                """;

        mockMvc.perform(post("/api/catalogo")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
    }
}