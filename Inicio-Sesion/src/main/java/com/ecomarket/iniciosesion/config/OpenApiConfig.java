package com.ecomarket.iniciosesion.config;

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
 *   <li>UI de Swagger: <b>http://localhost:8086/swagger-ui.html</b></li>
 *   <li>Especificación JSON: <b>http://localhost:8086/v3/api-docs</b></li>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI authOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("EcoMarket · Inicio-Sesion API")
                .description("Autenticación de clientes con JWT: iniciar y cerrar sesión, "
                        + "validar token, cambiar contraseña y cambiar correo.")
                .version("v1")
                .contact(new Contact().name("EcoMarket").email("soporte@ecomarket.cl")));
    }
}
