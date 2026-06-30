package com.ecomarket.carritocompra.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "items_carrito")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCarrito {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Relación con el carrito dueño. Muchos items pertenecen a un carrito. */
    @ManyToOne
    @JoinColumn(name = "carrito_id", nullable = false)
    private Carrito carrito;

    /** ID del producto en el catálogo (referencia por valor, no relación JPA). */
    @Column(nullable = false)
    private Long productoId;

    /** Snapshot del nombre, para mostrar el carrito sin llamar al catálogo. */
    @Column(nullable = false, length = 120)
    private String nombreProducto;

    /** Snapshot del precio al agregar. Precio histórico, exacto (BigDecimal). */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false)
    private Integer cantidad;

    /** Lógica de dominio: subtotal de la línea = precio × cantidad. */
    public BigDecimal calcularSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}
