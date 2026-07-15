package com.ecomarket.carritocompra.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ecomarket.carritocompra.dto.PedidoResponseDTO;
import com.ecomarket.carritocompra.exception.CarritoVacioException;
import com.ecomarket.carritocompra.exception.RecursoNoEncontradoException;
import com.ecomarket.carritocompra.model.EstadoPedido;
import com.ecomarket.carritocompra.service.CompraService;

/**
 * TIPO 2 - Prueba del CompraController (@WebMvcTest + MockMvc, sin BD).
 * Cubre confirmar, cancelar e historial con sus codigos HTTP.
 */
@WebMvcTest(CompraController.class)
class CompraControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private CompraService compraService;

    private PedidoResponseDTO pedido(EstadoPedido estado) {
        return new PedidoResponseDTO(1L, 1L, new BigDecimal("7980.00"), estado, null, List.of());
    }

    @Test
    void confirmar_devuelve200yPedido() throws Exception {
        when(compraService.confirmarCompra(1L)).thenReturn(pedido(EstadoPedido.CONFIRMADO));

        mockMvc.perform(post("/api/v1/compras/confirmar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":1}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"));
    }

    @Test
    void confirmar_carritoVacio_devuelve409() throws Exception {
        when(compraService.confirmarCompra(1L))
                .thenThrow(new CarritoVacioException("El carrito esta vacio"));

        mockMvc.perform(post("/api/v1/compras/confirmar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":1}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void confirmar_sinClienteId_devuelve400() throws Exception {
        mockMvc.perform(post("/api/v1/compras/confirmar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detallesValidacion.clienteId").exists());
    }

    @Test
    void cancelar_devuelve200yPedidoCancelado() throws Exception {
        when(compraService.cancelarPedido(1L)).thenReturn(pedido(EstadoPedido.CANCELADO));

        mockMvc.perform(post("/api/v1/compras/1/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADO"));
    }

    @Test
    void cancelar_pedidoInexistente_devuelve404() throws Exception {
        when(compraService.cancelarPedido(99L))
                .thenThrow(new RecursoNoEncontradoException("Pedido no encontrado con id: 99"));

        mockMvc.perform(post("/api/v1/compras/99/cancelar"))
                .andExpect(status().isNotFound());
    }

    @Test
    void historial_devuelve200yLista() throws Exception {
        when(compraService.historial(1L)).thenReturn(List.of(pedido(EstadoPedido.CONFIRMADO)));

        mockMvc.perform(get("/api/v1/compras/historial/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }
}
