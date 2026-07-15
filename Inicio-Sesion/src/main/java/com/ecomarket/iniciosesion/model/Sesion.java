package com.ecomarket.iniciosesion.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Sesion de un cliente: registra el token JWT emitido en el login.
 *
 * <p>Permite implementar "cerrar sesion": al hacer logout la sesion se marca
 * inactiva, y aunque el JWT siga firmado y sin expirar, ya no se acepta.
 */
@Entity
@Table(name = "sesiones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Token JWT emitido (columna larga porque los JWT superan los 255 caracteres). */
    @Column(nullable = false, length = 600, unique = true)
    private String token;

    @Column(nullable = false)
    private Long clienteId;

    @Column(nullable = false)
    private LocalDateTime fechaEmision;

    @Column(nullable = false)
    private LocalDateTime fechaExpiracion;

    /** true mientras la sesion este abierta; false tras el logout. */
    @Column(nullable = false)
    private Boolean activa;
}
