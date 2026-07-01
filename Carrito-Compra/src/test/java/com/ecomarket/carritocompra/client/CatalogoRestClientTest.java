package com.ecomarket.carritocompra.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.ecomarket.carritocompra.client.dto.ProductoCatalogoDTO;
import com.ecomarket.carritocompra.exception.CatalogoNoDisponibleException;
import com.ecomarket.carritocompra.exception.RecursoNoEncontradoException;
import com.ecomarket.carritocompra.exception.StockInsuficienteException;

/**
 * TIPO 1 - Prueba unitaria del CatalogoRestClient con MockRestServiceServer.
 * No levanta HTTP real: simula las respuestas del catalogo para verificar que el
 * cliente traduce cada resultado (200 / 404 / 409 / error de red) a la excepcion
 * de dominio correcta.
 */
class CatalogoRestClientTest {

    private MockRestServiceServer server;
    private CatalogoRestClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:8081");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new CatalogoRestClient(builder.build());
    }

    @Test
    void obtenerProducto_respuesta200_devuelveDTO() {
        server.expect(requestTo("http://localhost:8081/api/catalogo/5"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "{\"id\":5,\"nombre\":\"Arroz\",\"precio\":2740.00,\"stock\":100,\"estado\":\"DISPONIBLE\"}",
                        MediaType.APPLICATION_JSON));

        ProductoCatalogoDTO dto = client.obtenerProducto(5L);

        assertThat(dto.nombre()).isEqualTo("Arroz");
        assertThat(dto.precio()).isEqualByComparingTo("2740.00");
        server.verify();
    }

    @Test
    void obtenerProducto_respuesta404_lanzaRecursoNoEncontrado() {
        server.expect(requestTo("http://localhost:8081/api/catalogo/99"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.obtenerProducto(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void obtenerProducto_errorDeConexion_lanzaCatalogoNoDisponible() {
        server.expect(requestTo("http://localhost:8081/api/catalogo/5"))
                .andRespond(withException(new IOException("conexion rechazada")));

        assertThatThrownBy(() -> client.obtenerProducto(5L))
                .isInstanceOf(CatalogoNoDisponibleException.class);
    }

    @Test
    void reservar_respuesta409_lanzaStockInsuficiente() {
        server.expect(requestTo("http://localhost:8081/api/inventario/5/reservar?cantidad=2"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CONFLICT));

        assertThatThrownBy(() -> client.reservar(5L, 2))
                .isInstanceOf(StockInsuficienteException.class);
    }

    @Test
    void verificarDisponibilidad_respuesta200_devuelveTrue() {
        server.expect(requestTo("http://localhost:8081/api/inventario/5/disponibilidad?cantidad=2"))
                .andRespond(withSuccess("{\"disponible\":true}", MediaType.APPLICATION_JSON));

        assertThat(client.verificarDisponibilidad(5L, 2)).isTrue();
    }
}
