package com.ecomarket.catalogoinventario.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * TIPO 4 - Prueba de integracion OPCIONAL con MySQL real.
 * Requiere MySQL encendido. Usa el perfil "mysql" (application-mysql.properties)
 * y la BD dedicada ecomarket_catalogo_test.
 * Esta etiquetada con @Tag("mysql"): NO se ejecuta en 'mvn test' normal,
 * solo con el perfil Maven:  ./mvnw test -Pmysql-it
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mysql")
@Tag("mysql")
class CatalogoMySqlIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void crudCompleto_sobreMySql() throws Exception {
        // CREATE
        String body = """
                {"sku":"MYSQL-01","nombre":"Producto MySQL","descripcion":"d",
                 "categoria":"RECICLAJE","precio":4990.00,"stock":15}
                """;
        String respuesta = mockMvc.perform(post("/api/v1/productos")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("MYSQL-01"))
                .andReturn().getResponse().getContentAsString();

        Long id = com.jayway.jsonpath.JsonPath.parse(respuesta).read("$.id", Long.class);

        // READ
        mockMvc.perform(get("/api/v1/productos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Producto MySQL"));

        // DELETE
        mockMvc.perform(delete("/api/v1/productos/" + id))
                .andExpect(status().isNoContent());

        // READ tras borrar -> 404
        mockMvc.perform(get("/api/v1/productos/" + id))
                .andExpect(status().isNotFound());
    }
}