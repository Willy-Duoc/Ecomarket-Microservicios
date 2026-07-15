package com.ecomarket.catalogoinventario.config;

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
 *   <li>UI de Swagger: <b>http://localhost:8084/swagger-ui.html</b></li>
 *   <li>Especificación JSON: <b>http://localhost:8084/v3/api-docs</b></li>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI catalogoOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("EcoMarket · Catálogo-Inventario API")
                .description("Gestión del catálogo de productos y del stock (inventario). "
                        + "Incluye CRUD de productos y operaciones de stock: verificar, "
                        + "reservar, liberar y ajustar.")
                .version("v1")
                .contact(new Contact().name("EcoMarket").email("soporte@ecomarket.cl")));
    }
}
