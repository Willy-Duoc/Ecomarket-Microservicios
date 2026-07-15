package com.ecomarket.carritocompra.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

/**
 * Prueba unitaria del RestClientConfig: verifica que el bean del RestClient
 * se construye correctamente con la URL base dada (cubre el metodo @Bean).
 */
class RestClientConfigTest {

    @Test
    void catalogoRestClient_seConstruye() {
        RestClient restClient = new RestClientConfig().restClientCatalogo("http://localhost:8085");
        assertThat(restClient).isNotNull();
    }
}
