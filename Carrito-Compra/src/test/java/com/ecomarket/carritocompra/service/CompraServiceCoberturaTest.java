package com.ecomarket.carritocompra.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecomarket.carritocompra.client.CatalogoClient;
import com.ecomarket.carritocompra.dto.PedidoResponseDTO;
import com.ecomarket.carritocompra.exception.RecursoNoEncontradoException;
import com.ecomarket.carritocompra.mapper.CarritoMapper;
import com.ecomarket.carritocompra.model.EstadoPedido;
import com.ecomarket.carritocompra.model.ItemPedido;
import com.ecomarket.carritocompra.model.Pedido;
import com.ecomarket.carritocompra.repository.CarritoRepository;
import com.ecomarket.carritocompra.repository.PedidoRepository;

/**
 * TIPO 1 - Cobertura de los metodos restantes de CompraService:
 * cancelarPedido (exito, idempotente, 404) e historial.
 */
@ExtendWith(MockitoExtension.class)
class CompraServiceCoberturaTest {

    @Mock private CarritoRepository carritoRepository;
    @Mock private PedidoRepository pedidoRepository;
    @Mock private CarritoService carritoService;
    @Mock private CatalogoClient catalogoClient;

    private final CarritoMapper carritoMapper = new CarritoMapper();
    private CompraService compraService;

    @BeforeEach
    void setUp() {
        compraService = new CompraService(carritoRepository, pedidoRepository,
                carritoService, catalogoClient, carritoMapper);
    }

    private Pedido pedidoConfirmado() {
        Pedido pedido = Pedido.builder().id(1L).clienteId(1L)
                .total(new BigDecimal("7980.00")).estado(EstadoPedido.CONFIRMADO).build();
        pedido.getItems().add(ItemPedido.builder().id(1L).pedido(pedido).productoId(10L)
                .nombreProducto("Quinoa").precioUnitario(new BigDecimal("3990.00")).cantidad(2).build());
        return pedido;
    }

    @Test
    void cancelarPedido_confirmado_liberaStockyMarcaCancelado() {
        Pedido pedido = pedidoConfirmado();
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(pedido)).thenReturn(pedido);

        PedidoResponseDTO resp = compraService.cancelarPedido(1L);

        verify(catalogoClient).liberar(10L, 2);
        assertThat(resp.estado()).isEqualTo(EstadoPedido.CANCELADO);
    }

    @Test
    void cancelarPedido_yaCancelado_esIdempotenteyNoLibera() {
        Pedido pedido = pedidoConfirmado();
        pedido.setEstado(EstadoPedido.CANCELADO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        PedidoResponseDTO resp = compraService.cancelarPedido(1L);

        assertThat(resp.estado()).isEqualTo(EstadoPedido.CANCELADO);
        verify(catalogoClient, never()).liberar(anyLong(), anyInt());
        verify(pedidoRepository, never()).save(pedido);
    }

    @Test
    void cancelarPedido_inexistente_lanza404() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> compraService.cancelarPedido(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void historial_devuelvePedidosMapeados() {
        when(pedidoRepository.findByClienteIdOrderByFechaCreacionDesc(1L))
                .thenReturn(List.of(pedidoConfirmado()));

        List<PedidoResponseDTO> historial = compraService.historial(1L);

        assertThat(historial).hasSize(1);
        assertThat(historial.get(0).total()).isEqualByComparingTo("7980.00");
    }
}
