package com.ecomarket.iniciosesion.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

/**
 * TIPO 3 - Integracion con H2: usa los 5 clientes sembrados por el seed real.
 * Recorre login -> validar -> logout -> validar (invalido) de punta a punta.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegracionH2Test {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void flujoCompleto_loginValidarLogout() throws Exception {
        // 1) Login con un cliente sembrado
        String respuesta = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo":"ana.torres@ecomarket.cl","contrasena":"ecomarket123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn().getResponse().getContentAsString();

        // Extraemos el token con JsonPath (incluido en spring-boot-starter-test)
        String token = JsonPath.read(respuesta, "$.token");

        // 2) El token es valido mientras la sesion este activa
        mockMvc.perform(post("/api/v1/auth/validar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true));

        // 3) Logout cierra la sesion
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\"}"))
                .andExpect(status().isOk());

        // 4) Tras el logout, el mismo token deja de ser valido
        mockMvc.perform(post("/api/v1/auth/validar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(false));
    }

    @Test
    void login_conContrasenaMala_devuelve401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo":"ana.torres@ecomarket.cl","contrasena":"incorrecta"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_conCuentaInactiva_devuelve403() throws Exception {
        // Elena Soto esta sembrada con estado INACTIVO
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo":"elena.soto@ecomarket.cl","contrasena":"ecomarket123"}
                                """))
                .andExpect(status().isForbidden());
    }
}
