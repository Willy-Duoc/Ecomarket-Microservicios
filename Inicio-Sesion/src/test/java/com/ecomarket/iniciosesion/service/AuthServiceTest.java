package com.ecomarket.iniciosesion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.ecomarket.iniciosesion.dto.LoginResponseDTO;
import com.ecomarket.iniciosesion.dto.MensajeResponseDTO;
import com.ecomarket.iniciosesion.dto.ValidarTokenResponseDTO;
import com.ecomarket.iniciosesion.exception.ContrasenasNoCoincidenException;
import com.ecomarket.iniciosesion.exception.CorreoDuplicadoException;
import com.ecomarket.iniciosesion.exception.CredencialesInvalidasException;
import com.ecomarket.iniciosesion.exception.CuentaInactivaException;
import com.ecomarket.iniciosesion.exception.RecursoNoEncontradoException;
import com.ecomarket.iniciosesion.exception.TokenInvalidoException;
import com.ecomarket.iniciosesion.model.Cliente;
import com.ecomarket.iniciosesion.model.Sesion;
import com.ecomarket.iniciosesion.repository.ClienteRepository;
import com.ecomarket.iniciosesion.repository.SesionRepository;
import com.ecomarket.iniciosesion.security.JwtUtil;

/**
 * TIPO 1 - Pruebas unitarias del AuthService (JUnit 5 + Mockito, sin BD).
 * Los repositorios se mockean; el JwtUtil es real (logica pura con un secreto de prueba).
 * Cubre todas las reglas: login (ok, mal password, inactivo, inexistente),
 * logout, validar, cambiar contrasena y cambiar correo con todos sus errores.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String SECRET = "7b9b1d2e4f6a8c0e2d4f6a8b0c2e4f6a8b0c2e4f6a8b0c2e4f6a8b0c2e4f6a8b";

    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private SesionRepository sesionRepository;

    private final JwtUtil jwtUtil = new JwtUtil(SECRET, 86400000L);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private AuthService authService;
    private String hashCorrecto;

    @BeforeEach
    void setUp() {
        authService = new AuthService(clienteRepository, sesionRepository, jwtUtil);
        hashCorrecto = encoder.encode("ecomarket123");
    }

    private Cliente cliente(String estado) {
        return Cliente.builder()
                .id(1L).nombre("Ana").apellido("Torres")
                .correo("ana.torres@ecomarket.cl").telefono("+56911111111")
                .contrasena(hashCorrecto).estado(estado)
                .build();
    }

    // ── Iniciar sesion ────────────────────────────────────────────────────────

    @Test
    void login_conCredencialesCorrectas_devuelveTokenYGuardaSesion() {
        when(clienteRepository.findByCorreo("ana.torres@ecomarket.cl"))
                .thenReturn(Optional.of(cliente("ACTIVO")));
        when(sesionRepository.save(any(Sesion.class))).thenAnswer(inv -> inv.getArgument(0));

        LoginResponseDTO resp = authService.iniciarSesion("ana.torres@ecomarket.cl", "ecomarket123");

        assertThat(resp.token()).isNotBlank();
        assertThat(resp.clienteId()).isEqualTo(1L);
        assertThat(resp.nombre()).isEqualTo("Ana");
        assertThat(jwtUtil.esTokenValido(resp.token())).isTrue();
        verify(sesionRepository).save(any(Sesion.class));
    }

    @Test
    void login_conContrasenaIncorrecta_lanza401() {
        when(clienteRepository.findByCorreo("ana.torres@ecomarket.cl"))
                .thenReturn(Optional.of(cliente("ACTIVO")));

        assertThatThrownBy(() -> authService.iniciarSesion("ana.torres@ecomarket.cl", "incorrecta"))
                .isInstanceOf(CredencialesInvalidasException.class);

        verify(sesionRepository, never()).save(any());
    }

    @Test
    void login_conCorreoInexistente_lanza401() {
        when(clienteRepository.findByCorreo("noexiste@ecomarket.cl")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.iniciarSesion("noexiste@ecomarket.cl", "ecomarket123"))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void login_conCuentaInactiva_lanza403() {
        when(clienteRepository.findByCorreo("ana.torres@ecomarket.cl"))
                .thenReturn(Optional.of(cliente("INACTIVO")));

        assertThatThrownBy(() -> authService.iniciarSesion("ana.torres@ecomarket.cl", "ecomarket123"))
                .isInstanceOf(CuentaInactivaException.class);
    }

    // ── Cerrar sesion ─────────────────────────────────────────────────────────

    @Test
    void logout_conSesionActiva_laMarcaInactiva() {
        String token = jwtUtil.generarToken(1L, "ana.torres@ecomarket.cl");
        Sesion sesion = Sesion.builder().id(1L).token(token).clienteId(1L).activa(true)
                .fechaEmision(java.time.LocalDateTime.now())
                .fechaExpiracion(java.time.LocalDateTime.now().plusDays(1)).build();
        when(sesionRepository.findByToken(token)).thenReturn(Optional.of(sesion));
        when(sesionRepository.save(sesion)).thenReturn(sesion);

        MensajeResponseDTO resp = authService.cerrarSesion(token);

        assertThat(sesion.getActiva()).isFalse();
        assertThat(resp.mensaje()).contains("cerrada exitosamente");
    }

    @Test
    void logout_conSesionYaCerrada_esIdempotente() {
        String token = jwtUtil.generarToken(1L, "ana.torres@ecomarket.cl");
        Sesion sesion = Sesion.builder().id(1L).token(token).clienteId(1L).activa(false)
                .fechaEmision(java.time.LocalDateTime.now())
                .fechaExpiracion(java.time.LocalDateTime.now().plusDays(1)).build();
        when(sesionRepository.findByToken(token)).thenReturn(Optional.of(sesion));

        MensajeResponseDTO resp = authService.cerrarSesion(token);

        assertThat(resp.mensaje()).contains("ya estaba cerrada");
        verify(sesionRepository, never()).save(any());
    }

    @Test
    void logout_conTokenInvalido_lanza401() {
        assertThatThrownBy(() -> authService.cerrarSesion("token-basura"))
                .isInstanceOf(TokenInvalidoException.class);
    }

    @Test
    void logout_conTokenSinSesion_lanza401() {
        String token = jwtUtil.generarToken(1L, "ana.torres@ecomarket.cl");
        when(sesionRepository.findByToken(token)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.cerrarSesion(token))
                .isInstanceOf(TokenInvalidoException.class);
    }

    // ── Validar token ─────────────────────────────────────────────────────────

    @Test
    void validar_conTokenActivoYSesionActiva_devuelveValido() {
        String token = jwtUtil.generarToken(1L, "ana.torres@ecomarket.cl");
        Sesion sesion = Sesion.builder().token(token).clienteId(1L).activa(true)
                .fechaEmision(java.time.LocalDateTime.now())
                .fechaExpiracion(java.time.LocalDateTime.now().plusDays(1)).build();
        when(sesionRepository.findByToken(token)).thenReturn(Optional.of(sesion));

        ValidarTokenResponseDTO resp = authService.validarToken(token);

        assertThat(resp.valido()).isTrue();
        assertThat(resp.clienteId()).isEqualTo(1L);
        assertThat(resp.correo()).isEqualTo("ana.torres@ecomarket.cl");
    }

    @Test
    void validar_conTokenBasura_devuelveInvalido() {
        ValidarTokenResponseDTO resp = authService.validarToken("token-basura");
        assertThat(resp.valido()).isFalse();
    }

    @Test
    void validar_conSesionCerrada_devuelveInvalido() {
        String token = jwtUtil.generarToken(1L, "ana.torres@ecomarket.cl");
        Sesion sesion = Sesion.builder().token(token).clienteId(1L).activa(false)
                .fechaEmision(java.time.LocalDateTime.now())
                .fechaExpiracion(java.time.LocalDateTime.now().plusDays(1)).build();
        when(sesionRepository.findByToken(token)).thenReturn(Optional.of(sesion));

        assertThat(authService.validarToken(token).valido()).isFalse();
    }

    @Test
    void validar_conTokenSinSesionRegistrada_devuelveInvalido() {
        String token = jwtUtil.generarToken(1L, "ana.torres@ecomarket.cl");
        when(sesionRepository.findByToken(token)).thenReturn(Optional.empty());

        assertThat(authService.validarToken(token).valido()).isFalse();
    }

    // ── Cambiar contrasena ────────────────────────────────────────────────────

    @Test
    void cambiarContrasena_valida_actualizaElHash() {
        Cliente cliente = cliente("ACTIVO");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(cliente)).thenReturn(cliente);

        MensajeResponseDTO resp = authService.cambiarContrasena(
                1L, "ecomarket123", "nuevaClave1", "nuevaClave1");

        assertThat(resp.mensaje()).contains("actualizada");
        assertThat(encoder.matches("nuevaClave1", cliente.getContrasena())).isTrue();
    }

    @Test
    void cambiarContrasena_conActualIncorrecta_lanza401() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente("ACTIVO")));

        assertThatThrownBy(() -> authService.cambiarContrasena(
                1L, "incorrecta", "nuevaClave1", "nuevaClave1"))
                .isInstanceOf(CredencialesInvalidasException.class);

        verify(clienteRepository, never()).save(any());
    }

    @Test
    void cambiarContrasena_conRepeticionDistinta_lanza400() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente("ACTIVO")));

        assertThatThrownBy(() -> authService.cambiarContrasena(
                1L, "ecomarket123", "nuevaClave1", "otraClave2"))
                .isInstanceOf(ContrasenasNoCoincidenException.class);

        verify(clienteRepository, never()).save(any());
    }

    @Test
    void cambiarContrasena_clienteInexistente_lanza404() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.cambiarContrasena(
                99L, "x", "nuevaClave1", "nuevaClave1"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    // ── Cambiar correo ────────────────────────────────────────────────────────

    @Test
    void cambiarCorreo_valido_actualizaElCorreo() {
        Cliente cliente = cliente("ACTIVO");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByCorreo("nuevo@ecomarket.cl")).thenReturn(false);
        when(clienteRepository.save(cliente)).thenReturn(cliente);

        MensajeResponseDTO resp = authService.cambiarCorreo(1L, "ecomarket123", "nuevo@ecomarket.cl");

        assertThat(resp.mensaje()).contains("actualizado");
        assertThat(cliente.getCorreo()).isEqualTo("nuevo@ecomarket.cl");
    }

    @Test
    void cambiarCorreo_conContrasenaIncorrecta_lanza401() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente("ACTIVO")));

        assertThatThrownBy(() -> authService.cambiarCorreo(1L, "incorrecta", "nuevo@ecomarket.cl"))
                .isInstanceOf(CredencialesInvalidasException.class);

        verify(clienteRepository, never()).existsByCorreo(anyString());
    }

    @Test
    void cambiarCorreo_conCorreoYaUsado_lanza409() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente("ACTIVO")));
        when(clienteRepository.existsByCorreo("ocupado@ecomarket.cl")).thenReturn(true);

        assertThatThrownBy(() -> authService.cambiarCorreo(1L, "ecomarket123", "ocupado@ecomarket.cl"))
                .isInstanceOf(CorreoDuplicadoException.class);

        verify(clienteRepository, never()).save(any());
    }

    @Test
    void cambiarCorreo_clienteInexistente_lanza404() {
        when(clienteRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.cambiarCorreo(99L, "x", "nuevo@ecomarket.cl"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
