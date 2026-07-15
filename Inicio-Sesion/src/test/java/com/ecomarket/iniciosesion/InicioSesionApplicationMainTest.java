package com.ecomarket.iniciosesion;

import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

/** Cubre el metodo main() sin levantar la aplicacion. */
class InicioSesionApplicationMainTest {

    @Test
    void main_invocaSpringApplicationRun() {
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            InicioSesionApplication.main(new String[]{});
            mocked.verify(() -> SpringApplication.run(InicioSesionApplication.class, new String[]{}));
        }
    }
}
