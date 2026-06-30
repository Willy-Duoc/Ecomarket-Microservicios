package com.ecomarket.catalogoinventario.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecomarket.catalogoinventario.model.CategoriaProducto;
import com.ecomarket.catalogoinventario.model.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

     /** Busca por SKU exacto. Útil para validar unicidad. */
    Optional<Producto> findBySku(String sku);

    /** True si ya existe un producto con ese SKU (validación antes de crear). */
    boolean existsBySku(String sku);

    /** Búsqueda por nombre parcial e insensible a mayúsculas (barra de búsqueda). */
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    /** Filtra el catálogo por categoría. */
    List<Producto> findByCategoria(CategoriaProducto categoria);
}
