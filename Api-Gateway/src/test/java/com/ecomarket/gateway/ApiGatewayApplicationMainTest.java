package com.ecomarket.gateway;

import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

/**
 * Cubre el metodo main() del gateway sin levantar la aplicacion.
 */
class ApiGatewayApplicationMainTest {

    @Test
    void main_invocaSpringApplicationRun() {
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            ApiGatewayApplication.main(new String[]{});
            mocked.verify(() -> SpringApplication.run(ApiGatewayApplication.class, new String[]{}));
        }
    }
}
