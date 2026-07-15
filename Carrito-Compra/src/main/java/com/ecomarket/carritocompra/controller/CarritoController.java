package com.ecomarket.carritocompra.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.ecomarket.carritocompra.dto.AgregarItemRequestDTO;
import com.ecomarket.carritocompra.dto.CarritoResponseDTO;
import com.ecomarket.carritocompra.service.CarritoService;

import jakarta.validation.Valid;

// API del carrito. Se accede via gateway: http://localhost:8085/api/v1/carritos
// Al agregar un producto este servicio llama al Catalogo-Inventario (reserva stock).
@Tag(name = "Carrito", description = "Carrito de compras del cliente")
@RestController
@RequestMapping("/api/v1/carritos")
public class CarritoController {
    
    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    /**
     * Obtiene (o crea) el carrito activo de un cliente.
     * <p>Funciona aunque el catálogo esté caído (datos locales / snapshot).
     * @param clienteId identificador del cliente.
     * @return 200 con el carrito y su total.
     */
    @GetMapping("/{clienteId}")
    @Operation(summary = "Obtiene (o crea) el carrito activo del cliente")
    public ResponseEntity<CarritoResponseDTO> obtener(@PathVariable Long clienteId) {
        return ResponseEntity.ok(carritoService.obtenerCarrito(clienteId));
    }

    /**
     * Agrega un producto al carrito (reserva stock en el catálogo).
     * @param dto clienteId, productoId y cantidad (validados).
     * @return 200 con el carrito actualizado; 404 producto inexistente;
     *         409 sin stock; 503 catálogo no disponible; 400 datos inválidos.
     */
    @PostMapping("/items")
    @Operation(summary = "Agrega un producto al carrito y reserva stock (503 si el catálogo no responde)")
    public ResponseEntity<CarritoResponseDTO> agregar(@Valid @RequestBody AgregarItemRequestDTO dto) {
        return ResponseEntity.ok(
                carritoService.agregarProducto(dto.clienteId(), dto.productoId(), dto.cantidad()));
    }

    /**
     * Elimina una línea del carrito (libera el stock reservado).
     * @param clienteId cliente dueño del carrito.
     * @param itemId    identificador de la línea a eliminar.
     * @return 200 con el carrito actualizado; 404 si el item no existe.
     */
    @DeleteMapping("/{clienteId}/items/{itemId}")
    @Operation(summary = "Elimina una línea del carrito y libera su stock")
    public ResponseEntity<CarritoResponseDTO> eliminarItem(
            @PathVariable Long clienteId, @PathVariable Long itemId) {
        return ResponseEntity.ok(carritoService.eliminarItem(clienteId, itemId));
    }

    /**
     * Vacía el carrito (libera el stock de todas las líneas).
     * @param clienteId cliente dueño del carrito.
     * @return 200 con el carrito vacío.
     */
    @DeleteMapping("/{clienteId}")
    @Operation(summary = "Vacía el carrito y libera todo el stock reservado")
    public ResponseEntity<CarritoResponseDTO> vaciar(@PathVariable Long clienteId) {
        return ResponseEntity.ok(carritoService.vaciarCarrito(clienteId));
    }
}
