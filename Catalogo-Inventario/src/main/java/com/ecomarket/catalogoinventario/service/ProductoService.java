package com.ecomarket.catalogoinventario.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecomarket.catalogoinventario.dto.ProductoRequestDTO;
import com.ecomarket.catalogoinventario.dto.ProductoResponseDTO;
import com.ecomarket.catalogoinventario.exception.RecursoNoEncontradoException;
import com.ecomarket.catalogoinventario.exception.SkuDuplicadoException;
import com.ecomarket.catalogoinventario.mapper.ProductoMapper;
import com.ecomarket.catalogoinventario.model.CategoriaProducto;
import com.ecomarket.catalogoinventario.model.Producto;
import com.ecomarket.catalogoinventario.repository.ProductoRepository;

@Service
@Transactional(readOnly = true)
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;

    public ProductoService(ProductoRepository productoRepository, ProductoMapper productoMapper) {
        this.productoRepository = productoRepository;
        this.productoMapper = productoMapper;
    }

    /** Lista todo el catálogo. */
    public List<ProductoResponseDTO> listarTodos() {
        return productoRepository.findAll().stream()
                .map(productoMapper::aResponse)
                .toList();
    }

    /** Obtiene un producto por id o lanza 404. */
    public ProductoResponseDTO obtenerPorId(Long id) {
        Producto producto = buscarEntidad(id);
        return productoMapper.aResponse(producto);
    }

    /** Búsqueda por nombre parcial (barra de búsqueda del cliente). */
    public List<ProductoResponseDTO> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre).stream()
                .map(productoMapper::aResponse)
                .toList();
    }

    /** Filtra el catálogo por categoría. */
    public List<ProductoResponseDTO> listarPorCategoria(CategoriaProducto categoria) {
        return productoRepository.findByCategoria(categoria).stream()
                .map(productoMapper::aResponse)
                .toList();
    }

    /** Crea un producto nuevo validando que el SKU no exista. */
    @Transactional
    public ProductoResponseDTO crear(ProductoRequestDTO dto) {
        if (productoRepository.existsBySku(dto.sku())) {
            throw new SkuDuplicadoException("Ya existe un producto con SKU: " + dto.sku());
        }
        Producto creado = productoRepository.save(productoMapper.aEntidad(dto));
        return productoMapper.aResponse(creado);
    }

    /** Actualiza un producto existente (404 si no existe; 409 si el nuevo SKU choca). */
    @Transactional
    public ProductoResponseDTO actualizar(Long id, ProductoRequestDTO dto) {
        Producto existente = buscarEntidad(id);

        boolean cambioSku = !existente.getSku().equals(dto.sku());
        if (cambioSku && productoRepository.existsBySku(dto.sku())) {
            throw new SkuDuplicadoException("Ya existe un producto con SKU: " + dto.sku());
        }

        productoMapper.copiarSobre(existente, dto);
        return productoMapper.aResponse(productoRepository.save(existente));
    }

    /** Elimina un producto (404 si no existe). */
    @Transactional
    public void eliminar(Long id) {
        Producto existente = buscarEntidad(id);
        productoRepository.delete(existente);
    }

    /**
     * Localiza la entidad o lanza 404.
     */
    private Producto buscarEntidad(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Producto no encontrado con id: " + id));
    }

}
