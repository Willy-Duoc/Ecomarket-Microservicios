package com.ecomarket.catalogoinventario.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.ecomarket.catalogoinventario.dto.ProductoRequestDTO;
import com.ecomarket.catalogoinventario.dto.ProductoResponseDTO;
import com.ecomarket.catalogoinventario.model.CategoriaProducto;
import com.ecomarket.catalogoinventario.model.EstadoProducto;
import com.ecomarket.catalogoinventario.model.Producto;

/**
 * TIPO 1 - Prueba unitaria del ProductoMapper (logica pura, sin Spring).
 * Verifica la conversion entidad <-> DTO y la asignacion de estado por defecto.
 */
class ProductoMapperTest {

    private final ProductoMapper mapper = new ProductoMapper();

    private ProductoRequestDTO request(EstadoProducto estado) {
        return new ProductoRequestDTO("ALI-01", "Quinoa", "Saco 1kg",
                CategoriaProducto.ALIMENTOS_ORGANICOS, new BigDecimal("3990.00"), 50,
                "http://img", estado);
    }

    @Test
    void aResponse_copiaTodosLosCampos() {
        Producto p = Producto.builder()
                .id(1L).sku("ALI-01").nombre("Quinoa").descripcion("Saco 1kg")
                .categoria(CategoriaProducto.ALIMENTOS_ORGANICOS).precio(new BigDecimal("3990.00"))
                .stock(50).imagenUrl("http://img").estado(EstadoProducto.DISPONIBLE).build();

        ProductoResponseDTO dto = mapper.aResponse(p);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.sku()).isEqualTo("ALI-01");
        assertThat(dto.precio()).isEqualByComparingTo("3990.00");
        assertThat(dto.estado()).isEqualTo(EstadoProducto.DISPONIBLE);
    }

    @Test
    void aEntidad_sinEstado_asignaDisponiblePorDefecto() {
        Producto p = mapper.aEntidad(request(null));
        assertThat(p.getEstado()).isEqualTo(EstadoProducto.DISPONIBLE);
        assertThat(p.getSku()).isEqualTo("ALI-01");
    }

    @Test
    void aEntidad_conEstado_respetaElEstado() {
        Producto p = mapper.aEntidad(request(EstadoProducto.DESCONTINUADO));
        assertThat(p.getEstado()).isEqualTo(EstadoProducto.DESCONTINUADO);
    }

    @Test
    void copiarSobre_actualizaCamposEditables() {
        Producto destino = Producto.builder().sku("VIEJO").nombre("viejo").estado(EstadoProducto.AGOTADO).build();

        mapper.copiarSobre(destino, request(EstadoProducto.DISPONIBLE));

        assertThat(destino.getSku()).isEqualTo("ALI-01");
        assertThat(destino.getNombre()).isEqualTo("Quinoa");
        assertThat(destino.getEstado()).isEqualTo(EstadoProducto.DISPONIBLE);
    }

    @Test
    void copiarSobre_sinEstado_noPisaElEstadoActual() {
        Producto destino = Producto.builder().sku("VIEJO").estado(EstadoProducto.AGOTADO).build();

        mapper.copiarSobre(destino, request(null));

        assertThat(destino.getEstado()).isEqualTo(EstadoProducto.AGOTADO);
    }
}
