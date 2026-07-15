package com.ecomarket.iniciosesion.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

/**
 * TIPO 1 - Prueba directa del GlobalExceptionHandler: cubre 401/403/404/409/400/500.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private WebRequest request() {
        WebRequest req = mock(WebRequest.class);
        when(req.getDescription(false)).thenReturn("uri=/api/v1/auth/login");
        return req;
    }

    @Test
    void credencialesInvalidas_devuelve401() {
        ResponseEntity<ErrorResponse> resp =
                handler.noAutorizado(new CredencialesInvalidasException("mal"), request());
        assertThat(resp.getStatusCode().value()).isEqualTo(401);
        assertThat(resp.getBody().path()).isEqualTo("/api/v1/auth/login");
    }

    @Test
    void tokenInvalido_devuelve401() {
        ResponseEntity<ErrorResponse> resp =
                handler.noAutorizado(new TokenInvalidoException("token"), request());
        assertThat(resp.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void cuentaInactiva_devuelve403() {
        ResponseEntity<ErrorResponse> resp =
                handler.cuentaInactiva(new CuentaInactivaException("inactiva"), request());
        assertThat(resp.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void noEncontrado_devuelve404() {
        ResponseEntity<ErrorResponse> resp =
                handler.noEncontrado(new RecursoNoEncontradoException("no existe"), request());
        assertThat(resp.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void correoDuplicado_devuelve409() {
        ResponseEntity<ErrorResponse> resp =
                handler.correoDuplicado(new CorreoDuplicadoException("duplicado"), request());
        assertThat(resp.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void contrasenasNoCoinciden_devuelve400() {
        ResponseEntity<ErrorResponse> resp = handler.contrasenasNoCoinciden(
                new ContrasenasNoCoincidenException("no coinciden"), request());
        assertThat(resp.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void validacion_devuelve400ConDetalles() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult br = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(br);
        when(br.getFieldErrors()).thenReturn(List.of(new FieldError("dto", "correo", "obligatorio")));

        ResponseEntity<ErrorResponse> resp = handler.validacion(ex, request());

        assertThat(resp.getStatusCode().value()).isEqualTo(400);
        assertThat(resp.getBody().detallesValidacion()).containsEntry("correo", "obligatorio");
    }

    @Test
    void generico_devuelve500() {
        ResponseEntity<ErrorResponse> resp = handler.generico(new RuntimeException("boom"), request());
        assertThat(resp.getStatusCode().value()).isEqualTo(500);
    }
}
