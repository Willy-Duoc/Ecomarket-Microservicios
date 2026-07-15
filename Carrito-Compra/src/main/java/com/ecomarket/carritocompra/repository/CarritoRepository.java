package com.ecomarket.carritocompra.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecomarket.carritocompra.model.Carrito;

/**
 * Capa de acceso a datos de {@link Carrito} (patrón CSR → Repository).
 * La consulta se declara explícitamente con {@link Query}: el acceso a la BD
 * queda asignado a esta capa.
 */
@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    /** Devuelve el carrito activo del cliente (a lo sumo uno). */
    @Query("SELECT c FROM Carrito c WHERE c.clienteId = :clienteId AND c.activo = true")
    Optional<Carrito> findByClienteIdAndActivoTrue(@Param("clienteId") Long clienteId);
}
