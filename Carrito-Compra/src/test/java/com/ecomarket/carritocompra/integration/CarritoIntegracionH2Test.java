package com.ecomarket.carritocompra.integration;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.ecomarket.carritocompra.client.CatalogoClient;
import com.ecomarket.carritocompra.client.dto.ProductoCatalogoDTO;

/**
 * TIPO 3 - Prueba de integracion con H2 (@SpringBootTest + MockMvc).
 * Recorre controller -> service -> repository -> H2 en el flujo real del carrito.
 * El CatalogoClient se mockea (@MockitoBean): NO se necesita el catalogo real.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CarritoIntegracionH2Test {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CatalogoClient catalogoClient;

    @BeforeEach
    void stubCatalogo() {
        when(catalogoClient.obtenerProducto(5L))
                .thenReturn(new ProductoCatalogoDTO(5L, "Arroz Integral", new BigDecimal("2740.00"), 100, "DISPONIBLE"));
        // reservar / liberar / confirmar son void: por defecto no hacen nada (mock).
    }

    @Test
    void agregarYVerCarrito_flujoCompleto() throws Exception {
        String body = """
                {"clienteId":1,"productoId":5,"cantidad":2}
                """;

        mockMvc.perform(post("/api/v1/carritos/items")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].nombreProducto").value("Arroz Integral"))
                .andExpect(jsonPath("$.total").value(5480.00));

        mockMvc.perform(get("/api/v1/carritos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].cantidad").value(2));
    }

    @Test
    void confirmarCompra_generaPedido() throws Exception {
        mockMvc.perform(post("/api/v1/carritos/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":2,"productoId":5,"cantidad":1}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/compras/confirmar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":2}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"))
                .andExpect(jsonPath("$.total").value(2740.00));
    }

    @Test
    void confirmar_carritoVacio_devuelve409() throws Exception {
        when(catalogoClient.obtenerProducto(anyLong())).thenReturn(null);
        // Cliente 3 sin items
        mockMvc.perform(post("/api/v1/compras/confirmar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":3}
                                """))
                .andExpect(status().isConflict());
    }
}