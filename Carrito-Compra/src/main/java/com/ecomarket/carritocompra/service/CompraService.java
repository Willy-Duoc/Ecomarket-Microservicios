package com.ecomarket.carritocompra.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecomarket.carritocompra.client.CatalogoClient;
import com.ecomarket.carritocompra.dto.PedidoResponseDTO;
import com.ecomarket.carritocompra.exception.CarritoVacioException;
import com.ecomarket.carritocompra.exception.RecursoNoEncontradoException;
import com.ecomarket.carritocompra.mapper.CarritoMapper;
import com.ecomarket.carritocompra.model.Carrito;
import com.ecomarket.carritocompra.model.EstadoPedido;
import com.ecomarket.carritocompra.model.ItemPedido;
import com.ecomarket.carritocompra.model.Pedido;
import com.ecomarket.carritocompra.repository.CarritoRepository;
import com.ecomarket.carritocompra.repository.PedidoRepository;

@Service
public class CompraService {
    
    private final CarritoRepository carritoRepository;
    private final PedidoRepository pedidoRepository;
    private final CarritoService carritoService;
    private final CatalogoClient catalogoClient;
    private final CarritoMapper carritoMapper;

    public CompraService(CarritoRepository carritoRepository,
                         PedidoRepository pedidoRepository,
                         CarritoService carritoService,
                         CatalogoClient catalogoClient,
                         CarritoMapper carritoMapper) {
        this.carritoRepository = carritoRepository;
        this.pedidoRepository = pedidoRepository;
        this.carritoService = carritoService;
        this.catalogoClient = catalogoClient;
        this.carritoMapper = carritoMapper;
    }

    /** Confirma la compra del carrito activo y devuelve el pedido generado. */
    @Transactional
    public PedidoResponseDTO confirmarCompra(Long clienteId) {
        Carrito carrito = carritoService.obtenerOActivar(clienteId);

        if (carrito.getItems().isEmpty()) {
            throw new CarritoVacioException(
                    "No se puede confirmar la compra: el carrito del cliente " + clienteId + " está vacío");
        }

        // 1) Regla de negocio: los productos comprados se ELIMINAN del registro
        //    del catalogo-inventario al confirmar la compra
        carrito.getItems().forEach(i -> catalogoClient.eliminarProducto(i.getProductoId()));

        // 2) Construir el pedido con snapshots (precio histórico congelado)
        Pedido pedido = Pedido.builder()
                .clienteId(clienteId)
                .total(carrito.calcularTotal())
                .estado(EstadoPedido.CONFIRMADO)
                .build();

        carrito.getItems().forEach(i -> pedido.getItems().add(ItemPedido.builder()
                .pedido(pedido)
                .productoId(i.getProductoId())
                .nombreProducto(i.getNombreProducto())
                .precioUnitario(i.getPrecioUnitario())
                .cantidad(i.getCantidad())
                .build()));

        Pedido guardado = pedidoRepository.save(pedido);

        // 3) Cerrar el carrito
        carrito.setActivo(false);
        carritoRepository.save(carrito);

        return carritoMapper.aPedidoResponse(guardado);
    }

    /**
     * Cancela un pedido confirmado y restaura (libera) el stock de sus líneas.
     * @throws RecursoNoEncontradoException si el pedido no existe (404).
     */
    @Transactional
    public PedidoResponseDTO cancelarPedido(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Pedido no encontrado con id: " + pedidoId));

        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            return carritoMapper.aPedidoResponse(pedido); // idempotente: ya estaba cancelado
        }

        // Restaurar stock en el catálogo
        pedido.getItems().forEach(i -> catalogoClient.liberar(i.getProductoId(), i.getCantidad()));

        pedido.setEstado(EstadoPedido.CANCELADO);
        return carritoMapper.aPedidoResponse(pedidoRepository.save(pedido));
    }

    /** Historial de pedidos del cliente (más recientes primero). */
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> historial(Long clienteId) {
        return pedidoRepository.findByClienteIdOrderByFechaCreacionDesc(clienteId).stream()
                .map(carritoMapper::aPedidoResponse)
                .toList();
    }
}
