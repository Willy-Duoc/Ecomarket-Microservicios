package com.ecomarket.carritocompra.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecomarket.carritocompra.dto.ConfirmarCompraRequestDTO;
import com.ecomarket.carritocompra.dto.PedidoResponseDTO;
import com.ecomarket.carritocompra.service.CompraService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/compras")
public class CompraController {
    private final CompraService compraService;

    public CompraController(CompraService compraService) {
        this.compraService = compraService;
    }

    /**
     * Confirma la compra del carrito activo del cliente.
     * @param dto clienteId (validado).
     * @return 200 con el pedido generado; 409 si el carrito está vacío;
     *         503 si el catálogo no está disponible.
     */
    @PostMapping("/confirmar")
    public ResponseEntity<PedidoResponseDTO> confirmar(@Valid @RequestBody ConfirmarCompraRequestDTO dto) {
        return ResponseEntity.ok(compraService.confirmarCompra(dto.clienteId()));
    }

    /**
     * Cancela un pedido confirmado y restaura su stock en el catálogo.
     * @param pedidoId identificador del pedido a cancelar.
     * @return 200 con el pedido cancelado; 404 si el pedido no existe.
     */
    @PostMapping("/{pedidoId}/cancelar")
    public ResponseEntity<PedidoResponseDTO> cancelar(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(compraService.cancelarPedido(pedidoId));
    }

    /**
     * Devuelve el historial de pedidos de un cliente (más recientes primero).
     * @param clienteId identificador del cliente.
     * @return 200 con la lista de pedidos (posiblemente vacía).
     */
    @GetMapping("/historial/{clienteId}")
    public ResponseEntity<List<PedidoResponseDTO>> historial(@PathVariable Long clienteId) {
        return ResponseEntity.ok(compraService.historial(clienteId));
    }
}
