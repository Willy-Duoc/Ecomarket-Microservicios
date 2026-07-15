package com.ecomarket.iniciosesion.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
 * Logica de negocio de la autenticacion de clientes.
 *
 * <p>Reglas principales:
 * - Solo clientes con estado ACTIVO pueden iniciar sesion.
 * - Las contrasenas se comparan siempre contra el hash BCrypt.
 * - El logout marca la sesion como inactiva (el token deja de aceptarse).
 * - Cambiar contrasena exige la actual + nueva + repeticion identica.
 * - Cambiar correo exige la contrasena y que el nuevo correo este libre.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final ClienteRepository clienteRepository;
    private final SesionRepository sesionRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(ClienteRepository clienteRepository,
                       SesionRepository sesionRepository,
                       JwtUtil jwtUtil) {
        this.clienteRepository = clienteRepository;
        this.sesionRepository = sesionRepository;
        this.jwtUtil = jwtUtil;
    }

    /** Inicia sesion: valida credenciales y estado, emite el JWT y registra la sesion. */
    @Transactional
    public LoginResponseDTO iniciarSesion(String correo, String contrasena) {
        Cliente cliente = clienteRepository.findByCorreo(correo)
                .orElseThrow(() -> new CredencialesInvalidasException("Correo o contrasena incorrectos"));

        if (!"ACTIVO".equalsIgnoreCase(cliente.getEstado())) {
            throw new CuentaInactivaException("La cuenta esta inactiva. Contacte a soporte.");
        }

        if (!passwordEncoder.matches(contrasena, cliente.getContrasena())) {
            throw new CredencialesInvalidasException("Correo o contrasena incorrectos");
        }

        String token = jwtUtil.generarToken(cliente.getId(), cliente.getCorreo());
        LocalDateTime ahora = LocalDateTime.now();

        sesionRepository.save(Sesion.builder()
                .token(token)
                .clienteId(cliente.getId())
                .fechaEmision(ahora)
                .fechaExpiracion(LocalDateTime.ofInstant(
                        Instant.now().plusMillis(jwtUtil.getExpirationMs()), ZoneId.systemDefault()))
                .activa(true)
                .build());

        log.info("Login exitoso del cliente {} ({})", cliente.getId(), cliente.getCorreo());
        return new LoginResponseDTO(token, cliente.getId(), cliente.getNombre(),
                cliente.getApellido(), cliente.getCorreo(), jwtUtil.getExpirationMs());
    }

    /** Cierra la sesion: marca el token como inactivo (deja de aceptarse). */
    @Transactional
    public MensajeResponseDTO cerrarSesion(String token) {
        if (!jwtUtil.esTokenValido(token)) {
            throw new TokenInvalidoException("El token no es valido o ya expiro");
        }

        Sesion sesion = sesionRepository.findByToken(token)
                .orElseThrow(() -> new TokenInvalidoException("No existe una sesion para ese token"));

        if (Boolean.FALSE.equals(sesion.getActiva())) {
            return MensajeResponseDTO.de("La sesion ya estaba cerrada");
        }

        sesion.setActiva(false);
        sesionRepository.save(sesion);
        log.info("Logout del cliente {}", sesion.getClienteId());
        return MensajeResponseDTO.de("Sesion cerrada exitosamente");
    }

    /** Valida un token: firma + expiracion + que la sesion siga activa. */
    @Transactional(readOnly = true)
    public ValidarTokenResponseDTO validarToken(String token) {
        if (!jwtUtil.esTokenValido(token)) {
            return ValidarTokenResponseDTO.invalido();
        }

        boolean sesionActiva = sesionRepository.findByToken(token)
                .map(Sesion::getActiva)
                .orElse(false);

        if (!sesionActiva) {
            return ValidarTokenResponseDTO.invalido();
        }

        return new ValidarTokenResponseDTO(true,
                jwtUtil.obtenerClienteId(token), jwtUtil.obtenerCorreo(token));
    }

    /** Cambia la contrasena: valida la actual y que la nueva coincida con su repeticion. */
    @Transactional
    public MensajeResponseDTO cambiarContrasena(Long clienteId, String actual,
                                                String nueva, String repetir) {
        Cliente cliente = buscarCliente(clienteId);

        if (!passwordEncoder.matches(actual, cliente.getContrasena())) {
            throw new CredencialesInvalidasException("La contrasena actual es incorrecta");
        }

        if (!nueva.equals(repetir)) {
            throw new ContrasenasNoCoincidenException(
                    "La nueva contrasena y su repeticion no coinciden");
        }

        cliente.setContrasena(passwordEncoder.encode(nueva));
        clienteRepository.save(cliente);
        log.info("Cambio de contrasena del cliente {}", clienteId);
        return MensajeResponseDTO.de("Contrasena actualizada exitosamente");
    }

    /** Cambia el correo: valida la contrasena y que el nuevo correo no este en uso. */
    @Transactional
    public MensajeResponseDTO cambiarCorreo(Long clienteId, String contrasena, String nuevoCorreo) {
        Cliente cliente = buscarCliente(clienteId);

        if (!passwordEncoder.matches(contrasena, cliente.getContrasena())) {
            throw new CredencialesInvalidasException("La contrasena es incorrecta");
        }

        if (clienteRepository.existsByCorreo(nuevoCorreo)) {
            throw new CorreoDuplicadoException("El correo '" + nuevoCorreo + "' ya esta en uso");
        }

        cliente.setCorreo(nuevoCorreo);
        clienteRepository.save(cliente);
        log.info("Cambio de correo del cliente {}", clienteId);
        return MensajeResponseDTO.de("Correo actualizado exitosamente");
    }

    private Cliente buscarCliente(Long clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cliente no encontrado con id: " + clienteId));
    }
}
