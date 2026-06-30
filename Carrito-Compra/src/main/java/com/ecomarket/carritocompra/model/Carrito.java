package com.ecomarket.carritocompra.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "carritos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Carrito {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clienteId;

    @Column(nullable = false)
    private Boolean activo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaUltimaModificacion;

    /**
     * Items del carrito. {@code cascade = ALL} + {@code orphanRemoval = true}:
     * al guardar el carrito se guardan sus items, y al quitar un item de la lista
     * se elimina de la BD automáticamente.
     */
    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItemCarrito> items = new ArrayList<>();

    @PrePersist
    void alCrear() {
        LocalDateTime ahora = LocalDateTime.now();
        if (fechaCreacion == null) fechaCreacion = ahora;
        if (fechaUltimaModificacion == null) fechaUltimaModificacion = ahora;
        if (activo == null) activo = true;
    }

    /** Lógica de dominio: total del carrito = suma de subtotales de cada línea. */
    public BigDecimal calcularTotal() {
        return items.stream()
                .map(ItemCarrito::calcularSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
