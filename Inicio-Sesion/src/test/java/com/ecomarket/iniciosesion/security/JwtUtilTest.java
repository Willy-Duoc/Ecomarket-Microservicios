package com.ecomarket.iniciosesion.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.JwtException;

/**
 * TIPO 1 - Pruebas unitarias del JwtUtil (logica pura, sin Spring).
 * Verifica generacion, validacion, extraccion de claims y rechazo de tokens invalidos.
 */
class JwtUtilTest {

    private static final String SECRET = "7b9b1d2e4f6a8c0e2d4f6a8b0c2e4f6a8b0c2e4f6a8b0c2e4f6a8b0c2e4f6a8b";

    private final JwtUtil jwtUtil = new JwtUtil(SECRET, 86400000L);

    @Test
    void generarToken_produceTokenValidoConClaims() {
        String token = jwtUtil.generarToken(7L, "ana.torres@ecomarket.cl");

        assertThat(jwtUtil.esTokenValido(token)).isTrue();
        assertThat(jwtUtil.obtenerClienteId(token)).isEqualTo(7L);
        assertThat(jwtUtil.obtenerCorreo(token)).isEqualTo("ana.torres@ecomarket.cl");
        assertThat(jwtUtil.getExpirationMs()).isEqualTo(86400000L);
    }

    @Test
    void esTokenValido_conTokenBasura_devuelveFalse() {
        assertThat(jwtUtil.esTokenValido("token-basura")).isFalse();
        assertThat(jwtUtil.esTokenValido("")).isFalse();
    }

    @Test
    void esTokenValido_conTokenExpirado_devuelveFalse() {
        JwtUtil jwtExpirado = new JwtUtil(SECRET, -1000L); // expira en el pasado
        String token = jwtExpirado.generarToken(1L, "ana.torres@ecomarket.cl");

        assertThat(jwtExpirado.esTokenValido(token)).isFalse();
    }

    @Test
    void esTokenValido_conOtroSecreto_devuelveFalse() {
        JwtUtil otroSecreto = new JwtUtil(
                "otro-secreto-distinto-de-al-menos-32-caracteres-para-hmac", 86400000L);
        String tokenAjeno = otroSecreto.generarToken(1L, "ana.torres@ecomarket.cl");

        assertThat(jwtUtil.esTokenValido(tokenAjeno)).isFalse();
    }

    @Test
    void validarYObtenerClaims_conTokenInvalido_lanzaJwtException() {
        assertThatThrownBy(() -> jwtUtil.validarYObtenerClaims("token-basura"))
                .isInstanceOfAny(JwtException.class, IllegalArgumentException.class);
    }
}
