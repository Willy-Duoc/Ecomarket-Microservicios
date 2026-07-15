package com.ecomarket.iniciosesion.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecomarket.iniciosesion.model.Cliente;

/** Acceso a datos de Cliente. */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /** Busca un cliente por su correo de acceso (login). */
    Optional<Cliente> findByCorreo(String correo);

    /** True si ya existe un cliente con ese correo (para cambiar correo). */
    boolean existsByCorreo(String correo);
}
