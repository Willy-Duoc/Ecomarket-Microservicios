package com.ecomarket.carritocompra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

/**
 * Configuración de Swagger / OpenAPI (springdoc).
 *
 * <p>Documentación automática disponible en:
 * <ul>
 *   <li>UI de Swagger: <b>http://localhost:8083/swagger-ui.html</b></li>
 *   <li>Especificación JSON: <b>http://localhost:8083/v3/api-docs</b></li>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI carritoOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("EcoMarket · Carrito-Compra API")
                .description("Carrito de compras, generación de pedidos e historial. "
                        + "Se comunica por REST con el Catálogo-Inventario para reservar "
                        + "stock y, al confirmar la compra, eliminar los productos comprados.")
                .version("v1")
                .contact(new Contact().name("EcoMarket").email("soporte@ecomarket.cl")));
    }
}
