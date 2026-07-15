package com.ecomarket.carritocompra.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.ecomarket.carritocompra.client.dto.ProductoCatalogoDTO;
import com.ecomarket.carritocompra.exception.CatalogoNoDisponibleException;
import com.ecomarket.carritocompra.exception.RecursoNoEncontradoException;
import com.ecomarket.carritocompra.exception.StockInsuficienteException;

@Component
public class CatalogoRestClient implements CatalogoClient {

    private final RestClient restClient;

    public CatalogoRestClient(RestClient restClientCatalogo) {
        this.restClient = restClientCatalogo;
    }

    @Override
    public ProductoCatalogoDTO obtenerProducto(Long productoId) {
        try {
            return restClient.get()
                    .uri("/api/v1/productos/{id}", productoId)
                    .retrieve()
                    .body(ProductoCatalogoDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new RecursoNoEncontradoException("Producto no encontrado en el catálogo: " + productoId);
        } catch (ResourceAccessException e) {
            throw new CatalogoNoDisponibleException(
                    "No se pudo contactar al catálogo para obtener el producto " + productoId);
        }
    }

    @Override
    public boolean verificarDisponibilidad(Long productoId, int cantidad) {
        try {
            DisponibilidadDTO resp = restClient.get()
                    .uri(b -> b.path("/api/v1/inventario/{id}/disponibilidad")
                              .queryParam("cantidad", cantidad).build(productoId))
                    .retrieve()
                    .body(DisponibilidadDTO.class);
            return resp != null && Boolean.TRUE.equals(resp.disponible());
        } catch (HttpClientErrorException.NotFound e) {
            throw new RecursoNoEncontradoException("Producto no encontrado en el catálogo: " + productoId);
        } catch (ResourceAccessException e) {
            throw new CatalogoNoDisponibleException("No se pudo contactar al catálogo (disponibilidad)");
        }
    }

    @Override
    public void reservar(Long productoId, int cantidad) {
        try {
            restClient.post()
                    .uri(b -> b.path("/api/v1/inventario/{id}/reservar")
                              .queryParam("cantidad", cantidad).build(productoId))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.Conflict e) {
            throw new StockInsuficienteException(
                    "Stock insuficiente en el catálogo para el producto " + productoId);
        } catch (HttpClientErrorException.NotFound e) {
            throw new RecursoNoEncontradoException("Producto no encontrado en el catálogo: " + productoId);
        } catch (ResourceAccessException e) {
            throw new CatalogoNoDisponibleException("No se pudo contactar al catálogo (reservar)");
        }
    }

    @Override
    public void liberar(Long productoId, int cantidad) {
        try {
            restClient.post()
                    .uri(b -> b.path("/api/v1/inventario/{id}/liberar")
                              .queryParam("cantidad", cantidad).build(productoId))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound e) {
            // El producto ya fue eliminado del catalogo (compra confirmada):
            // no hay stock que restaurar, se ignora para que cancelar sea seguro.
        } catch (ResourceAccessException e) {
            throw new CatalogoNoDisponibleException("No se pudo contactar al catálogo (liberar)");
        }
    }

    @Override
    public void eliminarProducto(Long productoId) {
        // DELETE /api/v1/productos/{id}: la compra elimina el producto del catalogo
        try {
            restClient.delete()
                    .uri("/api/v1/productos/{id}", productoId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound e) {
            // Ya no existia: la operacion es idempotente, se ignora.
        } catch (ResourceAccessException e) {
            throw new CatalogoNoDisponibleException("No se pudo contactar al catálogo (eliminar producto)");
        }
    }

    /** DTO interno para deserializar {@code {"disponible": true}}. */
    private record DisponibilidadDTO(Boolean disponible) {
    }
}
