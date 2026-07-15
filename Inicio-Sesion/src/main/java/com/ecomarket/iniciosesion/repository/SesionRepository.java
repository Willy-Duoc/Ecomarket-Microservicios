package com.ecomarket.iniciosesion.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecomarket.iniciosesion.model.Sesion;

/**
 * Capa de acceso a datos de {@link Sesion} (patrón CSR → Repository).
 * La consulta por token se declara explícitamente con {@link Query}.
 */
@Repository
public interface SesionRepository extends JpaRepository<Sesion, Long> {

    /** Busca la sesión asociada a un token JWT. */
    @Query("SELECT s FROM Sesion s WHERE s.token = :token")
    Optional<Sesion> findByToken(@Param("token") String token);
}
