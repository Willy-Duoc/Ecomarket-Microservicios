package com.ecomarket.catalogoinventario.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/**
 * TIPO 1 - Prueba unitaria de la entidad Producto (hook @PrePersist alCrear).
 * Esta clase de test vive en el MISMO paquete que Producto para poder invocar
 * el metodo package-private alCrear().
 */
class ProductoTest {

    @Test
    void alCrear_sinValores_asignaFechaYEstadoPorDefecto() {
        Producto p = new Producto();

        p.alCrear();

        assertThat(p.getFechaCreacion()).isNotNull();
        assertThat(p.getEstado()).isEqualTo(EstadoProducto.DISPONIBLE);
    }

    @Test
    void alCrear_conValores_losRespeta() {
        LocalDateTime fecha = LocalDateTime.of(2020, 1, 1, 0, 0);
        Producto p = Producto.builder()
                .fechaCreacion(fecha)
                .estado(EstadoProducto.DESCONTINUADO)
                .build();

        p.alCrear();

        assertThat(p.getFechaCreacion()).isEqualTo(fecha);
        assertThat(p.getEstado()).isEqualTo(EstadoProducto.DESCONTINUADO);
    }
}
