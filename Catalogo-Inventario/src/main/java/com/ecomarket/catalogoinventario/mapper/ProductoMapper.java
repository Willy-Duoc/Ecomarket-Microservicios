package com.ecomarket.catalogoinventario.mapper;

import org.springframework.stereotype.Component;

import com.ecomarket.catalogoinventario.dto.ProductoRequestDTO;
import com.ecomarket.catalogoinventario.dto.ProductoResponseDTO;
import com.ecomarket.catalogoinventario.model.EstadoProducto;
import com.ecomarket.catalogoinventario.model.Producto;

@Component
public class ProductoMapper {
    
    public ProductoResponseDTO aResponse(Producto p) {
        return new ProductoResponseDTO(
                p.getId(),
                p.getSku(),
                p.getNombre(),
                p.getDescripcion(),
                p.getCategoria(),
                p.getPrecio(),
                p.getStock(),
                p.getImagenUrl(),
                p.getEstado(),
                p.getFechaCreacion()
        );
    }

    public Producto aEntidad(ProductoRequestDTO dto) {
        return Producto.builder()
                .sku(dto.sku())
                .nombre(dto.nombre())
                .descripcion(dto.descripcion())
                .categoria(dto.categoria())
                .precio(dto.precio())
                .stock(dto.stock())
                .imagenUrl(dto.imagenUrl())
                .estado(dto.estado() != null ? dto.estado() : EstadoProducto.DISPONIBLE)
                .build();
    }


    public void copiarSobre(Producto destino, ProductoRequestDTO dto) {
        destino.setSku(dto.sku());
        destino.setNombre(dto.nombre());
        destino.setDescripcion(dto.descripcion());
        destino.setCategoria(dto.categoria());
        destino.setPrecio(dto.precio());
        destino.setStock(dto.stock());
        destino.setImagenUrl(dto.imagenUrl());
        if (dto.estado() != null) {
            destino.setEstado(dto.estado());
        }
    }
}
