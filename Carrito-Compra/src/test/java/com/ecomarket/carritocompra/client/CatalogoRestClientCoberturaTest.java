package com.ecomarket.carritocompra.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
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

import com.ecomarket.carritocompra.exception.CatalogoNoDisponibleException;
import com.ecomarket.carritocompra.exception.RecursoNoEncontradoException;

/**
 * Cobertura de las ramas restantes de CatalogoRestClient:
 * reservar (exito, 404, error red), liberar y confirmar (exito y error red),
 * verificarDisponibilidad (false, 404, error red).
 */
class CatalogoRestClientCoberturaTest {

    private MockRestServiceServer server;
    private CatalogoRestClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:8084");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new CatalogoRestClient(builder.build());
    }

    @Test
    void reservar_ok_noLanza() {
        server.expect(requestTo("http://localhost:8084/api/v1/inventario/5/reservar?cantidad=2"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        assertThatCode(() -> client.reservar(5L, 2)).doesNotThrowAnyException();
        server.verify();
    }

    @Test
    void reservar_404_lanzaRecursoNoEncontrado() {
        server.expect(requestTo("http://localhost:8084/api/v1/inventario/9/reservar?cantidad=1"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.reservar(9L, 1))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void reservar_errorConexion_lanzaCatalogoNoDisponible() {
        server.expect(requestTo("http://localhost:8084/api/v1/inventario/5/reservar?cantidad=2"))
                .andRespond(withException(new IOException("caido")));

        assertThatThrownBy(() -> client.reservar(5L, 2))
                .isInstanceOf(CatalogoNoDisponibleException.class);
    }

    @Test
    void liberar_ok_noLanza() {
        server.expect(requestTo("http://localhost:8084/api/v1/inventario/5/liberar?cantidad=2"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        assertThatCode(() -> client.liberar(5L, 2)).doesNotThrowAnyException();
    }

    @Test
    void liberar_errorConexion_lanzaCatalogoNoDisponible() {
        server.expect(requestTo("http://localhost:8084/api/v1/inventario/5/liberar?cantidad=2"))
                .andRespond(withException(new IOException("caido")));

        assertThatThrownBy(() -> client.liberar(5L, 2))
                .isInstanceOf(CatalogoNoDisponibleException.class);
    }

    @Test
    void eliminarProducto_ok_noLanza() {
        server.expect(requestTo("http://localhost:8084/api/v1/productos/5"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());

        assertThatCode(() -> client.eliminarProducto(5L)).doesNotThrowAnyException();
    }

    @Test
    void eliminarProducto_404_esIdempotenteNoLanza() {
        server.expect(requestTo("http://localhost:8084/api/v1/productos/5"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatCode(() -> client.eliminarProducto(5L)).doesNotThrowAnyException();
    }

    @Test
    void eliminarProducto_errorConexion_lanzaCatalogoNoDisponible() {
        server.expect(requestTo("http://localhost:8084/api/v1/productos/5"))
                .andRespond(withException(new IOException("caido")));

        assertThatThrownBy(() -> client.eliminarProducto(5L))
                .isInstanceOf(CatalogoNoDisponibleException.class);
    }

    @Test
    void liberar_404_productoYaEliminado_noLanza() {
        // Tras una compra el producto ya no existe: liberar debe ignorar el 404
        server.expect(requestTo("http://localhost:8084/api/v1/inventario/5/liberar?cantidad=2"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatCode(() -> client.liberar(5L, 2)).doesNotThrowAnyException();
    }

    @Test
    void verificarDisponibilidad_false_cuandoNoHay() {
        server.expect(requestTo("http://localhost:8084/api/v1/inventario/5/disponibilidad?cantidad=2"))
                .andRespond(withSuccess("{\"disponible\":false}", MediaType.APPLICATION_JSON));

        assertThat(client.verificarDisponibilidad(5L, 2)).isFalse();
    }

    @Test
    void verificarDisponibilidad_respuestaVacia_devuelveFalse() {
        // Respuesta 200 sin cuerpo: el DTO deserializa a null -> el metodo devuelve false
        server.expect(requestTo("http://localhost:8084/api/v1/inventario/5/disponibilidad?cantidad=2"))
                .andRespond(withSuccess());

        assertThat(client.verificarDisponibilidad(5L, 2)).isFalse();
    }

    @Test
    void verificarDisponibilidad_404_lanzaRecursoNoEncontrado() {
        server.expect(requestTo("http://localhost:8084/api/v1/inventario/9/disponibilidad?cantidad=1"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.verificarDisponibilidad(9L, 1))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void verificarDisponibilidad_errorConexion_lanzaCatalogoNoDisponible() {
        server.expect(requestTo("http://localhost:8084/api/v1/inventario/5/disponibilidad?cantidad=2"))
                .andRespond(withException(new IOException("caido")));

        assertThatThrownBy(() -> client.verificarDisponibilidad(5L, 2))
                .isInstanceOf(CatalogoNoDisponibleException.class);
    }
}
