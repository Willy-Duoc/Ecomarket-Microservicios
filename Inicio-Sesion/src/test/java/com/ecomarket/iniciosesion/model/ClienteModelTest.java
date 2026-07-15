package com.ecomarket.iniciosesion.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/**
 * TIPO 1 - Prueba de las entidades: hook alCrear de Cliente y builder/setters de Sesion.
 */
class ClienteModelTest {

    @Test
    void cliente_alCrear_sinEstado_asignaActivo() {
        Cliente cliente = new Cliente();

        cliente.alCrear();

        assertThat(cliente.getEstado()).isEqualTo("ACTIVO");
    }

    @Test
    void cliente_alCrear_conEstado_loRespeta() {
        Cliente cliente = Cliente.builder().estado("INACTIVO").build();

        cliente.alCrear();

        assertThat(cliente.getEstado()).isEqualTo("INACTIVO");
    }

    @Test
    void cliente_constructorCompletoYSetters() {
        Cliente cliente = new Cliente(1L, "Ana", "Torres", "ana@ecomarket.cl",
                "+56911111111", "hash", "ACTIVO");
        cliente.setTelefono("+56900000000");

        assertThat(cliente.getId()).isEqualTo(1L);
        assertThat(cliente.getNombre()).isEqualTo("Ana");
        assertThat(cliente.getApellido()).isEqualTo("Torres");
        assertThat(cliente.getCorreo()).isEqualTo("ana@ecomarket.cl");
        assertThat(cliente.getTelefono()).isEqualTo("+56900000000");
        assertThat(cliente.getContrasena()).isEqualTo("hash");
    }

    @Test
    void sesion_constructorCompletoYSetters() {
        LocalDateTime ahora = LocalDateTime.now();
        Sesion sesion = new Sesion(1L, "token", 2L, ahora, ahora.plusDays(1), true);
        sesion.setActiva(false);

        assertThat(sesion.getId()).isEqualTo(1L);
        assertThat(sesion.getToken()).isEqualTo("token");
        assertThat(sesion.getClienteId()).isEqualTo(2L);
        assertThat(sesion.getFechaEmision()).isEqualTo(ahora);
        assertThat(sesion.getActiva()).isFalse();
    }
}
