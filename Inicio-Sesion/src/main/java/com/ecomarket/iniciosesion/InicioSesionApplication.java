package com.ecomarket.iniciosesion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio Inicio-Sesion (puerto 8082).
 * Autentica a los clientes de EcoMarket con JWT para que puedan interactuar
 * con Carrito-Compra y Catalogo-Inventario a traves del API Gateway.
 */
@SpringBootApplication
public class InicioSesionApplication {

    public static void main(String[] args) {
        SpringApplication.run(InicioSesionApplication.class, args);
    }
}
