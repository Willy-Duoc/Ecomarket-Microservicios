package com.ecomarket.catalogoinventario.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecomarket.catalogoinventario.exception.RecursoNoEncontradoException;
import com.ecomarket.catalogoinventario.exception.StockInsuficienteException;
import com.ecomarket.catalogoinventario.model.EstadoProducto;
import com.ecomarket.catalogoinventario.model.Producto;
import com.ecomarket.catalogoinventario.repository.ProductoRepository;

@Service
public class InventarioService {
    private final ProductoRepository productoRepository;

    public InventarioService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public boolean verificarDisponibilidad(Long productoId, int cantidad) {
        Producto producto = buscar(productoId);
        return producto.getStock() >= cantidad;
    }

   
    @Transactional
    public int reservar(Long productoId, int cantidad) {
        Producto producto = buscar(productoId);
        if (producto.getStock() < cantidad) {
            throw new StockInsuficienteException(
                    "Stock insuficiente para el producto " + productoId
                    + ". Disponible: " + producto.getStock() + ", solicitado: " + cantidad);
        }
        producto.setStock(producto.getStock() - cantidad);
        actualizarEstadoSegunStock(producto);
        productoRepository.save(producto);
        return producto.getStock();
    }

    @Transactional
    public int liberar(Long productoId, int cantidad) {
        Producto producto = buscar(productoId);
        producto.setStock(producto.getStock() + cantidad);
        actualizarEstadoSegunStock(producto);
        productoRepository.save(producto);
        return producto.getStock();
    }

 
    @Transactional(readOnly = true)
    public boolean confirmar(Long productoId, int cantidad) {
        buscar(productoId); // valida existencia (404 si no existe)
        return true;
    }

    @Transactional
    public int ajustarStock(Long productoId, int nuevaCantidad) {
        if (nuevaCantidad < 0) {
            throw new StockInsuficienteException("El stock no puede ser negativo");
        }
        Producto producto = buscar(productoId);
        producto.setStock(nuevaCantidad);
        actualizarEstadoSegunStock(producto);
        productoRepository.save(producto);
        return producto.getStock();
    }

    private void actualizarEstadoSegunStock(Producto producto) {
        if (producto.getEstado() == EstadoProducto.DESCONTINUADO) {
            return;
        }
        producto.setEstado(producto.getStock() == 0
                ? EstadoProducto.AGOTADO
                : EstadoProducto.DISPONIBLE);
    }

    private Producto buscar(Long productoId) {
        return productoRepository.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Producto no encontrado con id: " + productoId));
    }
}
