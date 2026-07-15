package com.ecomarket.iniciosesion.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecomarket.iniciosesion.model.Sesion;

/** Acceso a datos de Sesion (tokens emitidos). */
@Repository
public interface SesionRepository extends JpaRepository<Sesion, Long> {

    /** Busca la sesion asociada a un token JWT. */
    Optional<Sesion> findByToken(String token);
}
