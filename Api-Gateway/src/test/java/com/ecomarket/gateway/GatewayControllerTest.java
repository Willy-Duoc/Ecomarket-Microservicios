package com.ecomarket.gateway;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClient;

/**
 * Pruebas del GatewayController con MockMvc (standalone) + MockRestServiceServer.
 * El RestClient del gateway se enlaza a un servidor simulado, asi que no se
 * necesitan los microservicios reales: verificamos el enrutamiento a cada destino,
 * la propagacion del codigo/cuerpo y la ruta desconocida (404).
 */
class GatewayControllerTest {

    private MockRestServiceServer server;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        GatewayController controller = new GatewayController(
                builder.build(),
                "http://localhost:8084",   // catalogo
                "http://localhost:8083",   // carrito
                "http://localhost:8082");  // inicio-sesion (auth)
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void info_devuelveRutas() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servicio").value("EcoMarket API Gateway"));
    }

    @Test
    void enruta_catalogoGet_alCatalogo8084() throws Exception {
        server.expect(requestTo("http://localhost:8084/api/v1/productos/5"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"id\":5,\"nombre\":\"Arroz\"}", MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/api/v1/productos/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Arroz"));

        server.verify();
    }

    @Test
    void enruta_inventarioGet_conQueryString() throws Exception {
        server.expect(requestTo("http://localhost:8084/api/v1/inventario/5/disponibilidad?cantidad=2"))
                .andRespond(withSuccess("{\"disponible\":true}", MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/api/v1/inventario/5/disponibilidad?cantidad=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponible").value(true));
    }

    @Test
    void enruta_carritoPost_conCuerpo_alCarrito8083() throws Exception {
        server.expect(requestTo("http://localhost:8083/api/v1/carritos/items"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/api/v1/carritos/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":1,\"productoId\":5,\"cantidad\":2}"))
                .andExpect(status().isOk());

        server.verify();
    }

    @Test
    void enruta_comprasPost_alCarrito8083() throws Exception {
        server.expect(requestTo("http://localhost:8083/api/v1/compras/confirmar"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"estado\":\"CONFIRMADO\"}", MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/api/v1/compras/confirmar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":1}"))
                .andExpect(status().isOk());
    }

    @Test
    void enruta_propagaErrorDownstream_sinContentType() throws Exception {
        server.expect(requestTo("http://localhost:8084/api/v1/productos/999"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/v1/productos/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void enruta_authPost_alInicioSesion8082() throws Exception {
        server.expect(requestTo("http://localhost:8082/api/v1/auth/login"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"token\":\"jwt\"}", MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"ana.torres@ecomarket.cl\",\"contrasena\":\"ecomarket123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt"));

        server.verify();
    }

    @Test
    void rutaNoEnrutable_devuelve404() throws Exception {
        mockMvc.perform(get("/api/desconocido"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }
}
