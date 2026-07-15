package com.ecomarket.iniciosesion.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.ecomarket.iniciosesion.model.Cliente;
import com.ecomarket.iniciosesion.repository.ClienteRepository;

/**
 * Seed de datos: crea 5 clientes fijos al arrancar (si la tabla esta vacia).
 *
 * <p>Credenciales de prueba (todas con la contrasena "ecomarket123"):
 *  1. ana.torres@ecomarket.cl      (ACTIVO)
 *  2. bruno.rojas@ecomarket.cl     (ACTIVO)
 *  3. carla.munoz@ecomarket.cl     (ACTIVO)
 *  4. diego.perez@ecomarket.cl     (ACTIVO)
 *  5. elena.soto@ecomarket.cl      (INACTIVO - para probar el 403)
 */
@Component
public class CargaClientesIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CargaClientesIniciales.class);
    private static final String CONTRASENA_PRUEBA = "ecomarket123";

    private final ClienteRepository clienteRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public CargaClientesIniciales(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public void run(String... args) {
        if (clienteRepository.count() > 0) {
            log.info("Clientes ya inicializados ({}). No se siembra de nuevo.",
                    clienteRepository.count());
            return;
        }

        String hash = encoder.encode(CONTRASENA_PRUEBA);

        clienteRepository.save(cliente("Ana", "Torres", "ana.torres@ecomarket.cl", "+56911111111", hash, "ACTIVO"));
        clienteRepository.save(cliente("Bruno", "Rojas", "bruno.rojas@ecomarket.cl", "+56922222222", hash, "ACTIVO"));
        clienteRepository.save(cliente("Carla", "Munoz", "carla.munoz@ecomarket.cl", "+56933333333", hash, "ACTIVO"));
        clienteRepository.save(cliente("Diego", "Perez", "diego.perez@ecomarket.cl", "+56944444444", hash, "ACTIVO"));
        clienteRepository.save(cliente("Elena", "Soto", "elena.soto@ecomarket.cl", "+56955555555", hash, "INACTIVO"));

        log.info("Se sembraron {} clientes de prueba (contrasena: {}).",
                clienteRepository.count(), CONTRASENA_PRUEBA);
    }

    private Cliente cliente(String nombre, String apellido, String correo,
                            String telefono, String hash, String estado) {
        return Cliente.builder()
                .nombre(nombre).apellido(apellido).correo(correo)
                .telefono(telefono).contrasena(hash).estado(estado)
                .build();
    }
}
