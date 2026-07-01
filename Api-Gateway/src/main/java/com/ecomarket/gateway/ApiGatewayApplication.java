package com.ecomarket.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway de EcoMarket.
 *
 * <p>Punto de entrada unico (puerto 8080) que reenvia las peticiones a los
 * microservicios segun el prefijo de la ruta:
 * <ul>
 *   <li>{@code /api/catalogo/**} y {@code /api/inventario/**} -> Catalogo-Inventario (8081)</li>
 *   <li>{@code /api/carrito/**} y {@code /api/compras/**} -> Carrito-Compra (8082)</li>
 * </ul>
 *
 * <p>Es un gateway de enrutamiento ligero (proxy REST) construido con Spring MVC
 * para mantener el mismo stack de los microservicios. En un entorno productivo se
 * reemplazaria por Spring Cloud Gateway, pero la idea (un unico punto de entrada
 * que desacopla al cliente de las direcciones internas) es la misma.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
