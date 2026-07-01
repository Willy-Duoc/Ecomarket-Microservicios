package com.ecomarket.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuracion del gateway: expone el {@link RestClient} usado para reenviar
 * las peticiones a los microservicios.
 *
 * <p>Se crea con {@link RestClient#create()} (sin dependencias de otros beans),
 * lo que garantiza que el contexto arranque siempre. En las pruebas se sustituye
 * por un RestClient enlazado a un servidor simulado.
 */
@Configuration
public class GatewayConfig {

    @Bean
    public RestClient gatewayRestClient() {
        return RestClient.create();
    }
}
