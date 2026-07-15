package com.ecomarket.iniciosesion.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.ecomarket.iniciosesion.dto.CambiarContrasenaRequestDTO;
import com.ecomarket.iniciosesion.dto.CambiarCorreoRequestDTO;
import com.ecomarket.iniciosesion.dto.LoginRequestDTO;
import com.ecomarket.iniciosesion.dto.LoginResponseDTO;
import com.ecomarket.iniciosesion.dto.LogoutRequestDTO;
import com.ecomarket.iniciosesion.dto.MensajeResponseDTO;
import com.ecomarket.iniciosesion.dto.ValidarTokenRequestDTO;
import com.ecomarket.iniciosesion.dto.ValidarTokenResponseDTO;
import com.ecomarket.iniciosesion.service.AuthService;

import jakarta.validation.Valid;

/**
 * API REST de autenticacion de clientes.
 * Se accede via API Gateway: http://localhost:8081/api/v1/auth/...
 */
@Tag(name = "Autenticación", description = "Login, logout y gestión de credenciales de clientes con JWT")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // POST /api/v1/auth/login -> inicia sesion y devuelve el token JWT
    @PostMapping("/login")
    @Operation(summary = "Inicia sesión y devuelve un token JWT (401 credenciales inválidas, 403 cuenta inactiva)")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.iniciarSesion(dto.correo(), dto.contrasena()));
    }

    // POST /api/v1/auth/logout -> cierra la sesion del token entregado
    @PostMapping("/logout")
    @Operation(summary = "Cierra la sesión asociada a un token")
    public ResponseEntity<MensajeResponseDTO> logout(@Valid @RequestBody LogoutRequestDTO dto) {
        return ResponseEntity.ok(authService.cerrarSesion(dto.token()));
    }

    // POST /api/v1/auth/validar -> indica si un token es valido y de que cliente es
    @PostMapping("/validar")
    @Operation(summary = "Valida un token JWT e indica a qué cliente pertenece")
    public ResponseEntity<ValidarTokenResponseDTO> validar(
            @Valid @RequestBody ValidarTokenRequestDTO dto) {
        return ResponseEntity.ok(authService.validarToken(dto.token()));
    }

    // PUT /api/v1/auth/cambiar-contrasena -> exige actual + nueva + repeticion
    @PutMapping("/cambiar-contrasena")
    @Operation(summary = "Cambia la contraseña: requiere actual, nueva y repetición")
    public ResponseEntity<MensajeResponseDTO> cambiarContrasena(
            @Valid @RequestBody CambiarContrasenaRequestDTO dto) {
        return ResponseEntity.ok(authService.cambiarContrasena(
                dto.clienteId(), dto.contrasenaActual(),
                dto.nuevaContrasena(), dto.repetirContrasena()));
    }

    // PUT /api/v1/auth/cambiar-correo -> exige contrasena + nuevo correo
    @PutMapping("/cambiar-correo")
    @Operation(summary = "Cambia el correo: requiere contraseña y nuevo correo")
    public ResponseEntity<MensajeResponseDTO> cambiarCorreo(
            @Valid @RequestBody CambiarCorreoRequestDTO dto) {
        return ResponseEntity.ok(authService.cambiarCorreo(
                dto.clienteId(), dto.contrasena(), dto.nuevoCorreo()));
    }
}
