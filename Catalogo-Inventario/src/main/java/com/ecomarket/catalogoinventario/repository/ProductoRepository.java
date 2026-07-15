package com.ecomarket.catalogoinventario.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecomarket.catalogoinventario.model.CategoriaProducto;
import com.ecomarket.catalogoinventario.model.Producto;

/**
 * Capa de acceso a datos de {@link Producto} (patrón CSR → Repository).
 *
 * <p>Toda la manipulación de la base de datos vive aquí: las consultas se
 * declaran explícitamente con {@link Query} (JPQL), de modo que el acceso a
 * datos queda asignado a esta capa y el Service solo invoca estos métodos,
 * nunca la base de datos directamente.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /** Busca un producto por su SKU exacto. */
    @Query("SELECT p FROM Producto p WHERE p.sku = :sku")
    Optional<Producto> findBySku(@Param("sku") String sku);

    /** Indica si ya existe un producto con ese SKU (validación de unicidad). */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END "
            + "FROM Producto p WHERE p.sku = :sku")
    boolean existsBySku(@Param("sku") String sku);

    /** Búsqueda por nombre parcial e insensible a mayúsculas (barra de búsqueda). */
    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Producto> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    /** Filtra el catálogo por categoría. */
    @Query("SELECT p FROM Producto p WHERE p.categoria = :categoria")
    List<Producto> findByCategoria(@Param("categoria") CategoriaProducto categoria);
}
