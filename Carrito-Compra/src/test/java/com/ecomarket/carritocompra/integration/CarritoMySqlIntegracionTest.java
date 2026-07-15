package com.ecomarket.carritocompra.integration;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ecomarket.carritocompra.client.CatalogoClient;
import com.ecomarket.carritocompra.client.dto.ProductoCatalogoDTO;

/**
 * TIPO 4 - Prueba de integracion OPCIONAL con MySQL real (perfil "mysql").
 * Requiere MySQL encendido. BD dedicada: ecomarket_carrito_test.
 * @Tag("mysql"): solo corre con  ./mvnw test -Pmysql-it
 * El catalogo se sigue mockeando (probamos la persistencia del carrito en MySQL).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mysql")
@Tag("mysql")
class CarritoMySqlIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CatalogoClient catalogoClient;

    @Test
    void agregarYConfirmar_persisteEnMySql() throws Exception {
        when(catalogoClient.obtenerProducto(5L))
                .thenReturn(new ProductoCatalogoDTO(5L, "Arroz Integral", new BigDecimal("2740.00"), 100, "DISPONIBLE"));

        mockMvc.perform(post("/api/v1/carritos/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":50,"productoId":5,"cantidad":3}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(8220.00));

        mockMvc.perform(post("/api/v1/compras/confirmar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":50}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"));
    }
}