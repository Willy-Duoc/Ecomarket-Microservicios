package com.ecomarket.carritocompra.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecomarket.carritocompra.model.Pedido;

/**
 * Capa de acceso a datos de {@link Pedido} (patrón CSR → Repository).
 * La consulta del historial se declara explícitamente con {@link Query}.
 */
@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /** Historial de pedidos de un cliente, más recientes primero. */
    @Query("SELECT p FROM Pedido p WHERE p.clienteId = :clienteId ORDER BY p.fechaCreacion DESC")
    List<Pedido> findByClienteIdOrderByFechaCreacionDesc(@Param("clienteId") Long clienteId);
}
