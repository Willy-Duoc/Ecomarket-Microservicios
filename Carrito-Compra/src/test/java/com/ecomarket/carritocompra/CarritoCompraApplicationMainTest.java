package com.ecomarket.carritocompra;

import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

/**
 * Cubre el metodo main() de la clase de arranque sin levantar la aplicacion.
 */
class CarritoCompraApplicationMainTest {

    @Test
    void main_invocaSpringApplicationRun() {
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            CarritoCompraApplication.main(new String[]{});
            mocked.verify(() -> SpringApplication.run(CarritoCompraApplication.class, new String[]{}));
        }
    }
}
