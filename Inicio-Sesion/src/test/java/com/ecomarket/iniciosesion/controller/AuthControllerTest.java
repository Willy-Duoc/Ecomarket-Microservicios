package com.ecomarket.iniciosesion.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ecomarket.iniciosesion.dto.LoginResponseDTO;
import com.ecomarket.iniciosesion.dto.MensajeResponseDTO;
import com.ecomarket.iniciosesion.dto.ValidarTokenResponseDTO;
import com.ecomarket.iniciosesion.exception.CredencialesInvalidasException;
import com.ecomarket.iniciosesion.exception.CuentaInactivaException;
import com.ecomarket.iniciosesion.service.AuthService;

/**
 * TIPO 2 - Pruebas del AuthController (@WebMvcTest + MockMvc, sin BD).
 * Verifica rutas /api/v1/auth, codigos HTTP y cuerpos JSON.
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthService authService;

    @Test
    void login_valido_devuelve200ConToken() throws Exception {
        when(authService.iniciarSesion("ana.torres@ecomarket.cl", "ecomarket123"))
                .thenReturn(new LoginResponseDTO("jwt-token", 1L, "Ana", "Torres",
                        "ana.torres@ecomarket.cl", 86400000L));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo":"ana.torres@ecomarket.cl","contrasena":"ecomarket123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.clienteId").value(1));
    }

    @Test
    void login_conCredencialesMalas_devuelve401() throws Exception {
        when(authService.iniciarSesion(anyString(), anyString()))
                .thenThrow(new CredencialesInvalidasException("Correo o contrasena incorrectos"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo":"ana.torres@ecomarket.cl","contrasena":"mala"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void login_conCuentaInactiva_devuelve403() throws Exception {
        when(authService.iniciarSesion(anyString(), anyString()))
                .thenThrow(new CuentaInactivaException("La cuenta esta inactiva"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo":"elena.soto@ecomarket.cl","contrasena":"ecomarket123"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void login_conCorreoInvalido_devuelve400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo":"no-es-correo","contrasena":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detallesValidacion").exists());
    }

    @Test
    void logout_devuelve200() throws Exception {
        when(authService.cerrarSesion("jwt-token"))
                .thenReturn(MensajeResponseDTO.de("Sesion cerrada exitosamente"));

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"jwt-token"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Sesion cerrada exitosamente"));
    }

    @Test
    void validar_devuelve200ConDatos() throws Exception {
        when(authService.validarToken("jwt-token"))
                .thenReturn(new ValidarTokenResponseDTO(true, 1L, "ana.torres@ecomarket.cl"));

        mockMvc.perform(post("/api/v1/auth/validar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"jwt-token"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true))
                .andExpect(jsonPath("$.clienteId").value(1));
    }

    @Test
    void cambiarContrasena_devuelve200() throws Exception {
        when(authService.cambiarContrasena(1L, "actual1", "nueva12", "nueva12"))
                .thenReturn(MensajeResponseDTO.de("Contrasena actualizada exitosamente"));

        mockMvc.perform(put("/api/v1/auth/cambiar-contrasena")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":1,"contrasenaActual":"actual1",
                                 "nuevaContrasena":"nueva12","repetirContrasena":"nueva12"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Contrasena actualizada exitosamente"));
    }

    @Test
    void cambiarContrasena_conNuevaMuyCorta_devuelve400() throws Exception {
        mockMvc.perform(put("/api/v1/auth/cambiar-contrasena")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":1,"contrasenaActual":"actual1",
                                 "nuevaContrasena":"abc","repetirContrasena":"abc"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detallesValidacion.nuevaContrasena").exists());
    }

    @Test
    void cambiarCorreo_devuelve200() throws Exception {
        when(authService.cambiarCorreo(1L, "actual1", "nuevo@ecomarket.cl"))
                .thenReturn(MensajeResponseDTO.de("Correo actualizado exitosamente"));

        mockMvc.perform(put("/api/v1/auth/cambiar-correo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":1,"contrasena":"actual1","nuevoCorreo":"nuevo@ecomarket.cl"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Correo actualizado exitosamente"));
    }
}
