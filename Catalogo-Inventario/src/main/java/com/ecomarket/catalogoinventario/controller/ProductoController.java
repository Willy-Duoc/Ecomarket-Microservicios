package com.ecomarket.catalogoinventario.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.ecomarket.catalogoinventario.dto.ProductoRequestDTO;
import com.ecomarket.catalogoinventario.dto.ProductoResponseDTO;
import com.ecomarket.catalogoinventario.model.CategoriaProducto;
import com.ecomarket.catalogoinventario.service.ProductoService;

import jakarta.validation.Valid;

// API del catalogo de productos. Se accede via gateway: http://localhost:8081/api/v1/productos
// GET lista/busca, POST crea, PUT actualiza, DELETE elimina (tambien lo invoca
// Carrito-Compra al confirmar una compra).
@Tag(name = "Productos", description = "Catálogo de productos ecológicos: CRUD y búsqueda")
@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {
    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @Operation(summary = "Lista todos los productos del catálogo")
    public ResponseEntity<List<ProductoResponseDTO>> listar() {
        return ResponseEntity.ok(productoService.listarTodos());
    }
 
    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un producto por su id (404 si no existe)")
    public ResponseEntity<ProductoResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Busca productos por nombre parcial")
    public ResponseEntity<List<ProductoResponseDTO>> buscar(@RequestParam String nombre) {
        return ResponseEntity.ok(productoService.buscarPorNombre(nombre));
    }

    @GetMapping("/categoria/{categoria}")
    @Operation(summary = "Lista los productos de una categoría")
    public ResponseEntity<List<ProductoResponseDTO>> porCategoria(
            @PathVariable CategoriaProducto categoria) {
        return ResponseEntity.ok(productoService.listarPorCategoria(categoria));
    }

    @PostMapping
    @Operation(summary = "Crea un producto (201; 409 si el SKU ya existe)")
    public ResponseEntity<ProductoResponseDTO> crear(@Valid @RequestBody ProductoRequestDTO dto) {
        ProductoResponseDTO creado = productoService.crear(dto);
        return ResponseEntity
                .created(URI.create("/api/v1/productos/" + creado.id()))
                .body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza un producto existente")
    public ResponseEntity<ProductoResponseDTO> actualizar(
            @PathVariable Long id, @Valid @RequestBody ProductoRequestDTO dto) {
        return ResponseEntity.ok(productoService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un producto (204; también lo invoca Carrito al confirmar la compra)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
