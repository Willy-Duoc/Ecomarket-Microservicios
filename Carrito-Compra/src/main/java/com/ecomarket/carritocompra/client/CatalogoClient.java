package com.ecomarket.carritocompra.client;

import com.ecomarket.carritocompra.client.dto.ProductoCatalogoDTO;

public interface CatalogoClient {

    /** Obtiene los datos del producto (para el snapshot). 404 → no existe; caída → no disponible. */
    ProductoCatalogoDTO obtenerProducto(Long productoId);

    /** ¿Hay stock para esa cantidad? */
    boolean verificarDisponibilidad(Long productoId, int cantidad);

    /** Reserva (descuenta) stock. Lanza si no hay stock o el catálogo está caído. */
    void reservar(Long productoId, int cantidad);

    /** Libera (devuelve) stock previamente reservado. */
    void liberar(Long productoId, int cantidad);

    /** Confirma el consumo definitivo de stock al cerrar la compra. */
    void confirmar(Long productoId, int cantidad);
}
