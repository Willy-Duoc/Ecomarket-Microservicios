package com.ecomarket.catalogoinventario.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * TIPO 3 - Prueba de integracion con H2 (@SpringBootTest + MockMvc).
 * Levanta el contexto COMPLETO (controller -> service -> repository -> H2).
 * El seeder CargaDatosIniciales siembra 100 productos en H2 al arrancar.
 * @Transactional revierte los cambios de cada test para aislarlos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CatalogoIntegracionH2Test {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listar_devuelveCatalogoSembrado() throws Exception {
        mockMvc.perform(get("/api/v1/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("ALI-01"));
    }

    @Test
    void crearProducto_persisteYsePuedeObtener() throws Exception {
        String body = """
                {"sku":"TEST-01","nombre":"Producto Test","descripcion":"d",
                 "categoria":"ALIMENTOS_ORGANICOS","precio":1000.00,"stock":10}
                """;

        mockMvc.perform(post("/api/v1/productos")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.estado").value("DISPONIBLE"));
    }

    @Test
    void reservarStock_descuentaEnLaBaseDeDatos() throws Exception {
        // Producto 1 = ALI-01, stock inicial 100 (sembrado)
        mockMvc.perform(post("/api/v1/inventario/1/reservar").param("cantidad", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockRestante").value(95));

        mockMvc.perform(get("/api/v1/productos/1"))
                .andExpect(jsonPath("$.stock").value(95));
    }

    @Test
    void reservar_masDelStock_devuelve409() throws Exception {
        mockMvc.perform(post("/api/v1/inventario/1/reservar").param("cantidad", "100000"))
                .andExpect(status().isConflict());
    }

    @Test
    void obtenerInexistente_devuelve404() throws Exception {
        mockMvc.perform(get("/api/v1/productos/999999"))
                .andExpect(status().isNotFound());
    }
}