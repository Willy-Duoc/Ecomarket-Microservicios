package com.ecomarket.carritocompra.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecomarket.carritocompra.model.Carrito;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {
    Optional<Carrito> findByClienteIdAndActivoTrue(Long clienteId);
}