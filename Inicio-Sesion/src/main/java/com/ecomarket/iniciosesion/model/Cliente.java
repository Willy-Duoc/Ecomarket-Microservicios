package com.ecomarket.iniciosesion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Cliente de EcoMarket (unico tipo de usuario del sistema).
 *
 * <p>La contrasena se guarda SIEMPRE hasheada con BCrypt, nunca en texto plano.
 * El correo se usa como identificador de acceso (login) y debe ser unico.
 */
@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(nullable = false, length = 60)
    private String apellido;

    /** Correo de acceso: es el "usuario" con el que se inicia sesion. Unico. */
    @Column(nullable = false, unique = true, length = 120)
    private String correo;

    @Column(length = 20)
    private String telefono;

    /** Hash BCrypt de la contrasena (nunca la contrasena en claro). */
    @Column(nullable = false, length = 100)
    private String contrasena;

    /** Estado de la cuenta: ACTIVO o INACTIVO. Solo los ACTIVO pueden iniciar sesion. */
    @Column(nullable = false, length = 20)
    private String estado;

    @PrePersist
    void alCrear() {
        if (estado == null) {
            estado = "ACTIVO";
        }
    }
}
