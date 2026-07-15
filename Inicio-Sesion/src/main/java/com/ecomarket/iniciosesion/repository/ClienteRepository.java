package com.ecomarket.iniciosesion.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecomarket.iniciosesion.model.Cliente;

/**
 * Capa de acceso a datos de {@link Cliente} (patrón CSR → Repository).
 * Las consultas se declaran explícitamente con {@link Query}: el acceso a la
 * base de datos queda asignado a esta capa.
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /** Busca un cliente por su correo de acceso (login). */
    @Query("SELECT c FROM Cliente c WHERE c.correo = :correo")
    Optional<Cliente> findByCorreo(@Param("correo") String correo);

    /** Indica si ya existe un cliente con ese correo (para cambiar correo). */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END "
            + "FROM Cliente c WHERE c.correo = :correo")
    boolean existsByCorreo(@Param("correo") String correo);
}
