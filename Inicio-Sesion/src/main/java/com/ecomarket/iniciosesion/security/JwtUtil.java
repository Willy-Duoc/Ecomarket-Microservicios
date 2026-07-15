package com.ecomarket.iniciosesion.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Utilitario JWT: genera y valida los tokens de los clientes.
 *
 * <p>El token va firmado con HMAC-SHA usando el secreto de application.properties.
 * Es "stateless": cualquier servicio que conozca el secreto puede validarlo sin
 * llamar a este microservicio (por eso NO se crean dependencias sincronicas).
 */
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /** Genera un token con el correo como subject y el clienteId como claim. */
    public String generarToken(Long clienteId, String correo) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationMs);

        return Jwts.builder()
                .subject(correo)
                .claim("clienteId", clienteId)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(secretKey)
                .compact();
    }

    /** Valida la firma y expiracion, y devuelve los claims del token. */
    public Claims validarYObtenerClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** True si el token esta bien firmado y no ha expirado. */
    public boolean esTokenValido(String token) {
        try {
            validarYObtenerClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String obtenerCorreo(String token) {
        return validarYObtenerClaims(token).getSubject();
    }

    public Long obtenerClienteId(String token) {
        return validarYObtenerClaims(token).get("clienteId", Long.class);
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
