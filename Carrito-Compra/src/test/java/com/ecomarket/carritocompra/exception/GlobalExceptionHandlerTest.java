package com.ecomarket.carritocompra.exception;

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
 * Prueba unitaria directa del GlobalExceptionHandler del carrito.
 * Cubre 404, 409 (stock y carrito vacio), 503, 400 y 500.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private WebRequest request() {
        WebRequest req = mock(WebRequest.class);
        when(req.getDescription(false)).thenReturn("uri=/api/carrito/1");
        return req;
    }

    @Test
    void noEncontrado_devuelve404() {
        ResponseEntity<ErrorResponse> resp =
                handler.noEncontrado(new RecursoNoEncontradoException("no existe"), request());
        assertThat(resp.getStatusCode().value()).isEqualTo(404);
        assertThat(resp.getBody().path()).isEqualTo("/api/carrito/1");
    }

    @Test
    void conflicto_stockInsuficiente_devuelve409() {
        ResponseEntity<ErrorResponse> resp =
                handler.conflicto(new StockInsuficienteException("sin stock"), request());
        assertThat(resp.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void conflicto_carritoVacio_devuelve409() {
        ResponseEntity<ErrorResponse> resp =
                handler.conflicto(new CarritoVacioException("vacio"), request());
        assertThat(resp.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void catalogoCaido_devuelve503() {
        ResponseEntity<ErrorResponse> resp =
                handler.catalogoCaido(new CatalogoNoDisponibleException("caido"), request());
        assertThat(resp.getStatusCode().value()).isEqualTo(503);
    }

    @Test
    void validacion_devuelve400ConDetalles() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult br = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(br);
        when(br.getFieldErrors()).thenReturn(List.of(new FieldError("dto", "cantidad", "debe ser positiva")));

        ResponseEntity<ErrorResponse> resp = handler.validacion(ex, request());

        assertThat(resp.getStatusCode().value()).isEqualTo(400);
        assertThat(resp.getBody().detallesValidacion()).containsEntry("cantidad", "debe ser positiva");
    }

    @Test
    void generico_devuelve500() {
        ResponseEntity<ErrorResponse> resp =
                handler.generico(new RuntimeException("boom"), request());
        assertThat(resp.getStatusCode().value()).isEqualTo(500);
    }
}
