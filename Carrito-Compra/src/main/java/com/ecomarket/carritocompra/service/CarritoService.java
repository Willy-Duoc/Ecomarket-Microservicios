package com.ecomarket.carritocompra.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecomarket.carritocompra.client.CatalogoClient;
import com.ecomarket.carritocompra.client.dto.ProductoCatalogoDTO;
import com.ecomarket.carritocompra.dto.CarritoResponseDTO;
import com.ecomarket.carritocompra.exception.RecursoNoEncontradoException;
import com.ecomarket.carritocompra.mapper.CarritoMapper;
import com.ecomarket.carritocompra.model.Carrito;
import com.ecomarket.carritocompra.model.ItemCarrito;
import com.ecomarket.carritocompra.repository.CarritoRepository;

@Service
public class CarritoService {
    private final CarritoRepository carritoRepository;
    private final CatalogoClient catalogoClient;
    private final CarritoMapper carritoMapper;

    public CarritoService(CarritoRepository carritoRepository,
                          CatalogoClient catalogoClient,
                          CarritoMapper carritoMapper) {
        this.carritoRepository = carritoRepository;
        this.catalogoClient = catalogoClient;
        this.carritoMapper = carritoMapper;
    }

    /** Devuelve (o crea) el carrito activo del cliente como DTO. */
    @Transactional
    public CarritoResponseDTO obtenerCarrito(Long clienteId) {
        return carritoMapper.aResponse(obtenerOActivar(clienteId));
    }

    @Transactional
    public CarritoResponseDTO agregarProducto(Long clienteId, Long productoId, int cantidad) {
        // 1) Snapshot desde el catálogo (dueño del dato)
        ProductoCatalogoDTO producto = catalogoClient.obtenerProducto(productoId);

        // 2) Reservar stock ANTES de tocar el carrito (evita sobreventa)
        catalogoClient.reservar(productoId, cantidad);

        // 3) Actualizar el carrito local
        Carrito carrito = obtenerOActivar(clienteId);
        carrito.getItems().stream()
                .filter(i -> i.getProductoId().equals(productoId))
                .findFirst()
                .ifPresentOrElse(
                        existente -> existente.setCantidad(existente.getCantidad() + cantidad),
                        () -> carrito.getItems().add(ItemCarrito.builder()
                                .carrito(carrito)
                                .productoId(productoId)
                                .nombreProducto(producto.nombre())
                                .precioUnitario(producto.precio())
                                .cantidad(cantidad)
                                .build()));

        carrito.setFechaUltimaModificacion(LocalDateTime.now());
        return carritoMapper.aResponse(carritoRepository.save(carrito));
    }

    /**
     * Elimina una línea del carrito y libera su stock reservado.
     * @throws RecursoNoEncontradoException si el item no pertenece al carrito (404).
     */
    @Transactional
    public CarritoResponseDTO eliminarItem(Long clienteId, Long itemId) {
        Carrito carrito = obtenerOActivar(clienteId);

        ItemCarrito item = carrito.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Item " + itemId + " no encontrado en el carrito del cliente " + clienteId));

        // Libera en el catálogo lo que se había reservado
        catalogoClient.liberar(item.getProductoId(), item.getCantidad());

        carrito.getItems().remove(item); // orphanRemoval lo borra de la BD
        carrito.setFechaUltimaModificacion(LocalDateTime.now());
        return carritoMapper.aResponse(carritoRepository.save(carrito));
    }

    /** Vacía el carrito liberando el stock de todas sus líneas. */
    @Transactional
    public CarritoResponseDTO vaciarCarrito(Long clienteId) {
        Carrito carrito = obtenerOActivar(clienteId);

        carrito.getItems().forEach(i -> catalogoClient.liberar(i.getProductoId(), i.getCantidad()));
        carrito.getItems().clear();
        carrito.setFechaUltimaModificacion(LocalDateTime.now());
        return carritoMapper.aResponse(carritoRepository.save(carrito));
    }

    /** Busca el carrito activo o crea uno nuevo. Uso interno y por CompraService. */
    @Transactional
    public Carrito obtenerOActivar(Long clienteId) {
        return carritoRepository.findByClienteIdAndActivoTrue(clienteId)
                .orElseGet(() -> {
                    Carrito nuevo = Carrito.builder()
                            .clienteId(clienteId)
                            .activo(true)
                            .build();
                    return carritoRepository.save(nuevo);
                });
    }
}
