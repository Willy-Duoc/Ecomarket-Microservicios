package com.ecomarket.catalogoinventario.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

import com.ecomarket.catalogoinventario.dto.ProductoResponseDTO;
import com.ecomarket.catalogoinventario.model.CategoriaProducto;
import com.ecomarket.catalogoinventario.model.EstadoProducto;
import com.ecomarket.catalogoinventario.service.ProductoService;

/**
 * TIPO 2 - Cobertura de los endpoints restantes de ProductoController:
 * listar, buscar, categoria, actualizar y eliminar.
 */
@WebMvcTest(ProductoController.class)
class ProductoControllerCoberturaTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ProductoService productoService;

    private ProductoResponseDTO dto() {
        return new ProductoResponseDTO(1L, "ALI-01", "Quinoa Organica", "Saco 1kg",
                CategoriaProducto.ALIMENTOS_ORGANICOS, new BigDecimal("3990.00"),
                50, null, EstadoProducto.DISPONIBLE, null);
    }

    @Test
    void listar_devuelve200yLista() throws Exception {
        when(productoService.listarTodos()).thenReturn(List.of(dto()));

        mockMvc.perform(get("/api/catalogo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("ALI-01"));
    }

    @Test
    void buscar_devuelve200() throws Exception {
        when(productoService.buscarPorNombre("quinoa")).thenReturn(List.of(dto()));

        mockMvc.perform(get("/api/catalogo/buscar").param("nombre", "quinoa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Quinoa Organica"));
    }

    @Test
    void porCategoria_devuelve200() throws Exception {
        when(productoService.listarPorCategoria(eq(CategoriaProducto.ALIMENTOS_ORGANICOS)))
                .thenReturn(List.of(dto()));

        mockMvc.perform(get("/api/catalogo/categoria/ALIMENTOS_ORGANICOS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void actualizar_devuelve200() throws Exception {
        when(productoService.actualizar(eq(1L), any())).thenReturn(dto());

        String json = """
                {"sku":"ALI-01","nombre":"Quinoa Organica","categoria":"ALIMENTOS_ORGANICOS","precio":3990.00,"stock":50}
                """;

        mockMvc.perform(put("/api/catalogo/1")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void eliminar_devuelve204() throws Exception {
        doNothing().when(productoService).eliminar(1L);

        mockMvc.perform(delete("/api/catalogo/1"))
                .andExpect(status().isNoContent());
    }
}
