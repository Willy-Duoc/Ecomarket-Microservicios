package com.ecomarket.catalogoinventario.exception;

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
 * Prueba unitaria directa del GlobalExceptionHandler: invoca cada manejador y
 * verifica el codigo HTTP y el cuerpo. Cubre todas las ramas (404/409/409/400/500).
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private WebRequest request() {
        WebRequest req = mock(WebRequest.class);
        when(req.getDescription(false)).thenReturn("uri=/api/catalogo/1");
        return req;
    }

    @Test
    void manejarNoEncontrado_devuelve404() {
        ResponseEntity<ErrorResponse> resp =
                handler.manejarNoEncontrado(new RecursoNoEncontradoException("no existe"), request());

        assertThat(resp.getStatusCode().value()).isEqualTo(404);
        assertThat(resp.getBody().mensaje()).isEqualTo("no existe");
        assertThat(resp.getBody().path()).isEqualTo("/api/catalogo/1");
    }

    @Test
    void manejarStock_devuelve409() {
        ResponseEntity<ErrorResponse> resp =
                handler.manejarStock(new StockInsuficienteException("sin stock"), request());
        assertThat(resp.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void manejarSkuDuplicado_devuelve409() {
        ResponseEntity<ErrorResponse> resp =
                handler.manejarSkuDuplicado(new SkuDuplicadoException("sku repetido"), request());
        assertThat(resp.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void manejarValidacion_devuelve400ConDetalles() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult br = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(br);
        when(br.getFieldErrors()).thenReturn(List.of(new FieldError("dto", "precio", "debe ser positivo")));

        ResponseEntity<ErrorResponse> resp = handler.manejarValidacion(ex, request());

        assertThat(resp.getStatusCode().value()).isEqualTo(400);
        assertThat(resp.getBody().detallesValidacion()).containsEntry("precio", "debe ser positivo");
    }

    @Test
    void manejarGenerico_devuelve500() {
        ResponseEntity<ErrorResponse> resp =
                handler.manejarGenerico(new RuntimeException("boom"), request());
        assertThat(resp.getStatusCode().value()).isEqualTo(500);
        assertThat(resp.getBody().mensaje()).contains("boom");
    }
}
