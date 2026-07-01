package com.ecomarket.catalogoinventario;

import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

/**
 * Cubre el metodo main() de la clase de arranque SIN levantar la aplicacion:
 * se mockea SpringApplication.run de forma estatica y se verifica que main lo invoca.
 * Complementa al test contextLoads (que cubre la clase, no el main).
 */
class CatalogoInventarioApplicationMainTest {

    @Test
    void main_invocaSpringApplicationRun() {
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            CatalogoInventarioApplication.main(new String[]{});
            mocked.verify(() -> SpringApplication.run(CatalogoInventarioApplication.class, new String[]{}));
        }
    }
}
