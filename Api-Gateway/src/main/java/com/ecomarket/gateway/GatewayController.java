package com.ecomarket.gateway;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Controlador de enrutamiento del gateway.
 *
 * <p>Captura TODAS las peticiones bajo {@code /api/**} y las reenvia al
 * microservicio correspondiente conservando metodo, ruta, query y cuerpo, y
 * devolviendo tal cual el codigo HTTP y el cuerpo de la respuesta del servicio.
 *
 * <p>El {@link RestClient} se inyecta por constructor (bean {@code gatewayRestClient})
 * para que las pruebas puedan sustituirlo por uno enlazado a un servidor simulado.
 */
@RestController
public class GatewayController {

    private final RestClient restClient;
    private final String catalogoUrl;
    private final String carritoUrl;
    private final String authUrl;

    public GatewayController(
            RestClient gatewayRestClient,
            @Value("${gateway.catalogo-url}") String catalogoUrl,
            @Value("${gateway.carrito-url}") String carritoUrl,
            @Value("${gateway.auth-url}") String authUrl) {
        this.restClient = gatewayRestClient;
        this.catalogoUrl = catalogoUrl;
        this.carritoUrl = carritoUrl;
        this.authUrl = authUrl;
    }

    /** Pagina de informacion del gateway (util para verificar que esta arriba). */
    @GetMapping("/")
    public Map<String, Object> info() {
        return Map.of(
                "servicio", "EcoMarket API Gateway",
                "rutas", Map.of(
                        "/api/v1/auth/**", authUrl,
                        "/api/v1/productos/**", catalogoUrl,
                        "/api/v1/inventario/**", catalogoUrl,
                        "/api/v1/carritos/**", carritoUrl,
                        "/api/v1/compras/**", carritoUrl));
    }

    /** Reenvia cualquier peticion /api/** al microservicio que corresponda. */
    @RequestMapping("/api/**")
    public ResponseEntity<byte[]> enrutar(HttpServletRequest request,
                                          @RequestBody(required = false) byte[] body) {
        String destino = resolverDestino(request.getRequestURI());
        if (destino == null) {
            return ResponseEntity.status(404)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(("{\"error\":\"El gateway no tiene una ruta para "
                            + request.getRequestURI() + "\"}").getBytes());
        }

        String query = request.getQueryString();
        String url = destino + request.getRequestURI() + (query != null ? "?" + query : "");
        HttpMethod metodo = HttpMethod.valueOf(request.getMethod());

        RestClient.RequestBodySpec spec = restClient.method(metodo).uri(url);
        String contentType = request.getContentType();
        if (contentType != null) {
            spec = spec.header("Content-Type", contentType);
        }
        if (body != null && body.length > 0) {
            spec = spec.body(body);
        }

        // exchange() NO lanza excepcion ante 4xx/5xx: pasamos el estado y cuerpo tal cual.
        return spec.exchange((req, res) -> {
            byte[] contenido = res.getBody().readAllBytes();
            MediaType ct = res.getHeaders().getContentType();
            return ResponseEntity.status(res.getStatusCode())
                    .contentType(ct != null ? ct : MediaType.APPLICATION_JSON)
                    .body(contenido);
        });
    }

    /** Elige el microservicio destino segun el prefijo de la ruta. */
    private String resolverDestino(String uri) {
        // /api/v1/auth/** -> Inicio-Sesion (8086)
        if (uri.startsWith("/api/v1/auth")) {
            return authUrl;
        }
        // /api/v1/productos/** y /api/v1/inventario/** -> Catalogo-Inventario (8088)
        if (uri.startsWith("/api/v1/productos") || uri.startsWith("/api/v1/inventario")) {
            return catalogoUrl;
        }
        // /api/v1/carritos/** y /api/v1/compras/** -> Carrito-Compra (8087)
        if (uri.startsWith("/api/v1/carritos") || uri.startsWith("/api/v1/compras")) {
            return carritoUrl;
        }
        return null;
    }
}
