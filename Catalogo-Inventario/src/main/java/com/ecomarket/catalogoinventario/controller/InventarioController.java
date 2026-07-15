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

import com.ecomarket.catalogoinventario.service.InventarioService;

// API del inventario (stock). La consume Carrito-Compra por REST:
// verificar disponibilidad, reservar (al agregar al carrito) y liberar (al quitar/cancelar).
@RestController
@RequestMapping("/api/v1/inventario")
public class InventarioController {
    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @GetMapping("/{productoId}/disponibilidad")
    public ResponseEntity<Map<String, Boolean>> verificar(
            @PathVariable Long productoId, @RequestParam int cantidad) {
        boolean disponible = inventarioService.verificarDisponibilidad(productoId, cantidad);
        return ResponseEntity.ok(Map.of("disponible", disponible));
    }

    @PostMapping("/{productoId}/reservar")
    public ResponseEntity<Map<String, Integer>> reservar(
            @PathVariable Long productoId, @RequestParam int cantidad) {
        int restante = inventarioService.reservar(productoId, cantidad);
        return ResponseEntity.ok(Map.of("stockRestante", restante));
    }

    @PostMapping("/{productoId}/liberar")
    public ResponseEntity<Map<String, Integer>> liberar(
            @PathVariable Long productoId, @RequestParam int cantidad) {
        int restante = inventarioService.liberar(productoId, cantidad);
        return ResponseEntity.ok(Map.of("stockRestante", restante));
    }

    @PostMapping("/{productoId}/confirmar")
    public ResponseEntity<Map<String, Boolean>> confirmar(
            @PathVariable Long productoId, @RequestParam int cantidad) {
        boolean ok = inventarioService.confirmar(productoId, cantidad);
        return ResponseEntity.ok(Map.of("confirmado", ok));
    }

    @PutMapping("/{productoId}/stock")
    public ResponseEntity<Map<String, Integer>> ajustar(
            @PathVariable Long productoId, @RequestParam int nuevaCantidad) {
        int actual = inventarioService.ajustarStock(productoId, nuevaCantidad);
        return ResponseEntity.ok(Map.of("stockActual", actual));
    }
}
