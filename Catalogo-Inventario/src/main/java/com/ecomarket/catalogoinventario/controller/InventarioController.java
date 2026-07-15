package com.ecomarket.catalogoinventario.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.ecomarket.catalogoinventario.service.InventarioService;

// API del inventario (stock). La consume Carrito-Compra por REST:
// verificar disponibilidad, reservar (al agregar al carrito) y liberar (al quitar/cancelar).
@Tag(name = "Inventario", description = "Operaciones de stock consumidas por el Carrito")
@RestController
@RequestMapping("/api/v1/inventario")
public class InventarioController {
    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @GetMapping("/{productoId}/disponibilidad")
    @Operation(summary = "Verifica si hay stock suficiente para una cantidad")
    public ResponseEntity<Map<String, Boolean>> verificar(
            @PathVariable Long productoId, @RequestParam int cantidad) {
        boolean disponible = inventarioService.verificarDisponibilidad(productoId, cantidad);
        return ResponseEntity.ok(Map.of("disponible", disponible));
    }

    @PostMapping("/{productoId}/reservar")
    @Operation(summary = "Reserva (descuenta) stock (409 si no alcanza)")
    public ResponseEntity<Map<String, Integer>> reservar(
            @PathVariable Long productoId, @RequestParam int cantidad) {
        int restante = inventarioService.reservar(productoId, cantidad);
        return ResponseEntity.ok(Map.of("stockRestante", restante));
    }

    @PostMapping("/{productoId}/liberar")
    @Operation(summary = "Libera (devuelve) stock reservado")
    public ResponseEntity<Map<String, Integer>> liberar(
            @PathVariable Long productoId, @RequestParam int cantidad) {
        int restante = inventarioService.liberar(productoId, cantidad);
        return ResponseEntity.ok(Map.of("stockRestante", restante));
    }

    @PostMapping("/{productoId}/confirmar")
    @Operation(summary = "Confirma el consumo de stock")
    public ResponseEntity<Map<String, Boolean>> confirmar(
            @PathVariable Long productoId, @RequestParam int cantidad) {
        boolean ok = inventarioService.confirmar(productoId, cantidad);
        return ResponseEntity.ok(Map.of("confirmado", ok));
    }

    @PutMapping("/{productoId}/stock")
    @Operation(summary = "Ajuste manual del stock (acción del gerente)")
    public ResponseEntity<Map<String, Integer>> ajustar(
            @PathVariable Long productoId, @RequestParam int nuevaCantidad) {
        int actual = inventarioService.ajustarStock(productoId, nuevaCantidad);
        return ResponseEntity.ok(Map.of("stockActual", actual));
    }
}
