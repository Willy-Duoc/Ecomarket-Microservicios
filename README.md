# Ecomarket SPA · E-commerce

**Autor:** Williams Contreras  
**Asignatura:** Desarrollo Fullstack 1

---

Plataforma de e-commerce de productos ecológicos y sostenibles, construida con una
arquitectura de **microservicios** sobre Spring Boot. Este repositorio contiene tres
microservicios de negocio (cada uno dueño de sus datos) y un **API Gateway** que
actúa como punto de entrada único:

| Servicio | Puerto | Responsabilidad | Base de datos |
|---|---|---|---|
| **Api-Gateway** | 8081 | Punto de entrada único; enruta las peticiones a los microservicios | — (sin BD) |
| **Inicio-Sesion** | 8082 | Autenticación de clientes con **JWT**: login, logout, cambio de contraseña y correo | `ecomarket_iniciosesion` |
| **Carrito-Compra** | 8083 | Carrito de compras, pedidos e historial | `ecomarket_carrito` |
| **Catalogo-Inventario** | 8084 | Catálogo de productos y gestión de stock | `ecomarket_catalogo` |

Cada servicio es **dueño exclusivo de sus datos** y tiene su propia base de datos
(patrón *Database per Service*). El Carrito nunca accede a la base del Catálogo:
le consulta por REST y guarda una copia histórica (*snapshot*) del nombre y precio
de cada producto en el momento de agregarlo. Así el carrito y el historial siguen
funcionando aunque el catálogo esté caído.

---

## 1. Arquitectura del proyecto

### 1.1 Comunicación entre servicios

El cliente (Postman) habla **solo con el API Gateway** (8081), que reenvía cada
petición al microservicio correcto según el prefijo de la ruta. Los microservicios
se comunican entre sí por REST.

```
                          Cliente  (Postman / navegador)
                                        │
                                        ▼
                        ┌───────────────────────────────┐
                        │     API Gateway   ·   :8081    │   ← punto de entrada único
                        └───────────────┬───────────────┘
           ┌────────────────────────────┼────────────────────────────┐
           │                            │                            │
      /api/v1/auth             /api/v1/carritos              /api/v1/productos
           │                   /api/v1/compras               /api/v1/inventario
           ▼                            ▼                            ▼
 ┌───────────────────┐       ┌───────────────────┐       ┌────────────────────┐
 │   Inicio-Sesion   │       │   Carrito-Compra  │       │ Catalogo-Inventario│
 │      :8082        │       │      :8083        │       │       :8084        │
 │   JWT · login     │       │ carrito · pedidos │       │  productos · stock │
 └─────────┬─────────┘       └─────────┬─────────┘       └─────────┬──────────┘
           │                           │   REST: reservar /        │
           │                           │   liberar / eliminar ───► │
           ▼                           ▼                           ▼
    ┌─────────────┐            ┌─────────────┐            ┌─────────────┐
    │    MySQL    │            │    MySQL    │            │    MySQL    │
    │ iniciosesion│            │   carrito   │            │   catalogo  │
    └─────────────┘            └─────────────┘            └─────────────┘
```

El **Gateway** desacopla al cliente de las direcciones internas: no necesita conocer
los puertos 8084/8083, solo el 8081. Es un enrutador REST ligero (proxy) hecho con
Spring MVC; en producción se reemplazaría por Spring Cloud Gateway, pero cumple el
mismo rol de *punto de entrada único*.

El Catálogo no conoce al Carrito (no depende de él). El Carrito sí depende del
Catálogo, pero a través de una interfaz (`CatalogoClient`) que aísla el dominio de
los detalles de HTTP. Si el catálogo no responde, el Carrito devuelve **503**
(servicio no disponible) en vez de un error genérico.

**Inicio-Sesion no crea dependencias síncronas:** emite tokens **JWT firmados**
(stateless). Ningún microservicio necesita llamar a Inicio-Sesion para operar;
la validación del token es local (firma HMAC) y el gateway solo enruta.

**Regla de negocio de la compra:** al confirmar la compra, los productos comprados
**se eliminan del registro** del Catalogo-Inventario (lo hace el Carrito vía
`DELETE /api/v1/productos/{id}`).

### 1.2 Arquitectura interna por capas

Ambos servicios siguen la misma estructura **Controller → Service → Repository → Model**,
con DTOs de entrada/salida y un manejador global de excepciones:

```
Controller   Traduce HTTP ↔ llamadas al servicio. No tiene lógica de negocio.
Service      Lógica de negocio y reglas (SKU único, anti-sobreventa, etc.).
Repository   Acceso a datos (Spring Data JPA genera el SQL).
Model        Entidades JPA (tablas) y enums del dominio.
DTO          Objetos de transporte (separan el contrato HTTP del esquema de BD).
Mapper       Convierte entidad ↔ DTO.
Exception    Excepciones de dominio + GlobalExceptionHandler (códigos HTTP uniformes).
```

### 1.3 Estructura de carpetas y archivos

```
Ecomarket-Microservicios/
│
├── README.md
├── POSTMAN.md                           # Guía detallada de pruebas en Postman
├── EcoMarket.postman_collection.json    # Colección importable en Postman (vía gateway)
│
├── Api-Gateway/                         # API Gateway (puerto 8081)
│   ├── pom.xml
│   └── src/main/java/com/ecomarket/gateway/
│       ├── ApiGatewayApplication.java   # Clase main (arranque)
│       └── GatewayController.java        # Enruta /api/** a los microservicios
│
├── Inicio-Sesion/                       # Microservicio de autenticación (puerto 8082)
│   ├── pom.xml                           # Incluye JJWT (JWT) y spring-security-crypto (BCrypt)
│   └── src/
│       ├── main/java/com/ecomarket/iniciosesion/
│       │   ├── InicioSesionApplication.java
│       │   ├── model/        Cliente.java, Sesion.java
│       │   ├── repository/   ClienteRepository, SesionRepository
│       │   ├── dto/          Login/Logout/Validar/CambiarContrasena/CambiarCorreo DTOs
│       │   ├── security/     JwtUtil.java (genera y valida JWT)
│       │   ├── service/      AuthService.java
│       │   ├── controller/   AuthController.java   # /api/v1/auth
│       │   ├── exception/    GlobalExceptionHandler + excepciones (401/403/404/409)
│       │   └── config/       CargaClientesIniciales.java  # Seed de 5 clientes
│       └── test/             8 clases de prueba (unitarias + MockMvc + integración H2)
│
├── Catalogo-Inventario/                 # Microservicio 1 (puerto 8084)
│   ├── pom.xml
│   ├── mvnw  /  mvnw.cmd                 # Maven Wrapper (no requiere Maven instalado)
│   └── src/
│       ├── main/
│       │   ├── java/com/ecomarket/catalogoinventario/
│       │   │   ├── CatalogoInventarioApplication.java   # Clase main (arranque)
│       │   │   │
│       │   │   ├── model/
│       │   │   │   ├── Producto.java                    # Entidad principal
│       │   │   │   ├── CategoriaProducto.java           # Enum (10 categorías)
│       │   │   │   └── EstadoProducto.java              # Enum (DISPONIBLE/AGOTADO/DESCONTINUADO)
│       │   │   │
│       │   │   ├── dto/
│       │   │   │   ├── ProductoRequestDTO.java          # Entrada (con validaciones)
│       │   │   │   └── ProductoResponseDTO.java         # Salida
│       │   │   │
│       │   │   ├── repository/
│       │   │   │   └── ProductoRepository.java          # JpaRepository + query methods
│       │   │   │
│       │   │   ├── mapper/
│       │   │   │   └── ProductoMapper.java              # Entidad ↔ DTO
│       │   │   │
│       │   │   ├── service/
│       │   │   │   ├── ProductoService.java             # CRUD y búsquedas del catálogo
│       │   │   │   └── InventarioService.java           # Reservar/liberar/confirmar stock
│       │   │   │
│       │   │   ├── controller/
│       │   │   │   ├── ProductoController.java          # /api/v1/productos
│       │   │   │   └── InventarioController.java        # /api/v1/inventario
│       │   │   │
│       │   │   ├── exception/
│       │   │   │   ├── GlobalExceptionHandler.java      # @RestControllerAdvice
│       │   │   │   ├── ErrorResponse.java               # Cuerpo de error uniforme
│       │   │   │   ├── RecursoNoEncontradoException.java
│       │   │   │   ├── SkuDuplicadoException.java
│       │   │   │   └── StockInsuficienteException.java
│       │   │   │
│       │   │   └── config/
│       │   │       └── CargaDatosIniciales.java         # Siembra 100 productos al arrancar
│       │   │
│       │   └── resources/
│       │       └── application.properties               # Config MySQL, puerto 8084
│       │
│       └── test/
│           ├── java/com/ecomarket/catalogoinventario/
│           │   ├── CatalogoInventarioApplicationTests.java
│           │   ├── service/
│           │   │   ├── ProductoServiceTest.java          # Unit (Mockito)
│           │   │   └── InventarioServiceTest.java        # Unit (Mockito)
│           │   ├── controller/
│           │   │   └── ProductoControllerTest.java       # @WebMvcTest + MockMvc
│           │   └── integration/
│           │       ├── CatalogoIntegracionH2Test.java    # @SpringBootTest + H2
│           │       └── CatalogoMySqlIntegracionTest.java # @Tag("mysql"), opcional
│           └── resources/
│               ├── application.properties                # H2 en memoria (perfil test)
│               └── application-mysql.properties          # MySQL de pruebas (perfil mysql)
│
└── Carrito-Compra/                      # Microservicio 2 (puerto 8083)
    ├── pom.xml
    ├── mvnw  /  mvnw.cmd
    └── src/
        ├── main/
        │   ├── java/com/ecomarket/carritocompra/
        │   │   ├── CarritoCompraApplication.java
        │   │   │
        │   │   ├── model/
        │   │   │   ├── Carrito.java                      # Entidad: carrito activo
        │   │   │   ├── ItemCarrito.java                  # Línea del carrito (snapshot)
        │   │   │   ├── Pedido.java                       # Pedido confirmado
        │   │   │   ├── ItemPedido.java                   # Línea del pedido (snapshot)
        │   │   │   └── EstadoPedido.java                 # Enum (CONFIRMADO/CANCELADO)
        │   │   │
        │   │   ├── dto/
        │   │   │   ├── AgregarItemRequestDTO.java        # Entrada: agregar al carrito
        │   │   │   ├── ConfirmarCompraRequestDTO.java    # Entrada: confirmar compra
        │   │   │   ├── CarritoResponseDTO.java           # Salida: carrito + total
        │   │   │   ├── ItemCarritoResponseDTO.java
        │   │   │   ├── PedidoResponseDTO.java
        │   │   │   └── ItemPedidoResponseDTO.java
        │   │   │
        │   │   ├── client/                               # Comunicación con el Catálogo
        │   │   │   ├── CatalogoClient.java               # Interfaz (puerto)
        │   │   │   ├── CatalogoRestClient.java           # Implementación REST
        │   │   │   └── dto/
        │   │   │       └── ProductoCatalogoDTO.java      # Vista mínima del producto remoto
        │   │   │
        │   │   ├── config/
        │   │   │   └── RestClientConfig.java             # Bean RestClient con timeouts
        │   │   │
        │   │   ├── repository/
        │   │   │   ├── CarritoRepository.java
        │   │   │   └── PedidoRepository.java
        │   │   │
        │   │   ├── mapper/
        │   │   │   └── CarritoMapper.java
        │   │   │
        │   │   ├── service/
        │   │   │   ├── CarritoService.java               # Agregar/eliminar/vaciar carrito
        │   │   │   └── CompraService.java                # Confirmar/cancelar/historial
        │   │   │
        │   │   ├── controller/
        │   │   │   ├── CarritoController.java            # /api/v1/carritos
        │   │   │   └── CompraController.java             # /api/v1/compras
        │   │   │
        │   │   └── exception/
        │   │       ├── GlobalExceptionHandler.java
        │   │       ├── ErrorResponse.java
        │   │       ├── RecursoNoEncontradoException.java
        │   │       ├── StockInsuficienteException.java
        │   │       ├── CarritoVacioException.java
        │   │       └── CatalogoNoDisponibleException.java # → HTTP 503
        │   │
        │   └── resources/
        │       └── application.properties                # Config MySQL, puerto 8083, catalogo.base-url
        │
        └── test/
            ├── java/com/ecomarket/carritocompra/
            │   ├── CarritoCompraApplicationTests.java
            │   ├── service/
            │   │   ├── CarritoServiceTest.java            # Unit (Mockito)
            │   │   └── CompraServiceTest.java             # Unit (Mockito)
            │   ├── controller/
            │   │   └── CarritoControllerTest.java         # @WebMvcTest + MockMvc
            │   └── integration/
            │       ├── CarritoIntegracionH2Test.java      # @SpringBootTest + H2
            │       └── CarritoMySqlIntegracionTest.java   # @Tag("mysql"), opcional
            └── resources/
                ├── application.properties                 # H2 en memoria
                └── application-mysql.properties           # MySQL de pruebas
```

---

## 2. Dependencias utilizadas

### 2.1 Plataforma

- **Java 21**
- **Spring Boot 4.1.0** (heredado del `spring-boot-starter-parent`)
- **Maven** (vía Maven Wrapper incluido: `mvnw` / `mvnw.cmd`)

> Nota sobre Spring Boot 4: por la nueva modularización, el starter web se llama
> `spring-boot-starter-webmvc` (antes `-web`) e incluye Tomcat, Jackson y `RestClient`.

### 2.2 Dependencias de ejecución (ambos servicios)

| Dependencia | Para qué sirve |
|---|---|
| `spring-boot-starter-webmvc` | API REST: controladores, Tomcat embebido, Jackson (JSON) y `RestClient`. |
| `spring-boot-starter-data-jpa` | Persistencia con Hibernate + Spring Data (repositorios). |
| `spring-boot-starter-validation` | Validación de DTOs (`@Valid`, `@NotBlank`, `@NotNull`, `@Positive`, etc.). |
| `mysql-connector-j` | Driver JDBC de MySQL (scope `runtime`). |
| `lombok` | Genera getters/setters/constructores/builder (scope `optional`). |

### 2.3 Dependencias de pruebas (ambos servicios)

| Dependencia | Para qué sirve |
|---|---|
| `spring-boot-starter-test` | JUnit 5, Mockito, AssertJ, JsonPath. |
| `spring-boot-starter-webmvc-test` | Soporte de `@WebMvcTest` y `MockMvc` (test slice de la capa web). |
| `h2` | Base de datos en memoria para las pruebas de integración (scope `test`). |

### 2.4 Plugins de build

- `spring-boot-maven-plugin` – empaqueta el JAR ejecutable (excluye Lombok del artefacto final).
- `maven-compiler-plugin` – registra Lombok como *annotation processor*.
- `maven-surefire-plugin` – por defecto **excluye** las pruebas etiquetadas `@Tag("mysql")`.

---

## 3. Requisitos previos

- **JDK 21** instalado (`java -version` debe mostrar 21).
- **MySQL** en ejecución en `localhost:3306` con usuario `root` y sin contraseña
  (configuración típica de XAMPP). Si usas otra contraseña, cámbiala en cada
  `src/main/resources/application.properties`.
- No necesitas instalar Maven: usa el wrapper `./mvnw` (Linux/Mac) o `mvnw.cmd` (Windows).

> Las bases de datos `ecomarket_catalogo` y `ecomarket_carrito` no necesitan crearse
> a mano si agregas `?createDatabaseIfNotExist=true` a la URL del datasource. Si tu
> `application.properties` no lo incluye, créalas manualmente en MySQL antes de arrancar.

---

## 4. Cómo compilar el proyecto

Cada microservicio se compila por separado, desde su propia carpeta.

**Catálogo-Inventario:**
```bash
cd Catalogo-Inventario
./mvnw clean compile        # solo compila
./mvnw clean package        # compila + ejecuta tests + genera el JAR en target/
```

**Carrito-Compra:**
```bash
cd Carrito-Compra
./mvnw clean package
```

En Windows usa `mvnw.cmd` en lugar de `./mvnw`.
Para saltar las pruebas durante el empaquetado: `./mvnw clean package -DskipTests`.

---

## 5. Cómo ejecutar el proyecto

El orden importa: **primero el Catálogo**, luego el Carrito y por último el Gateway
(cada uno en su propia terminal).

**1) Arrancar el Catálogo (puerto 8084):**
```bash
cd Catalogo-Inventario
./mvnw spring-boot:run
```
Al iniciar, siembra automáticamente **100 productos** (10 categorías × 10 productos).
Verás en consola: `Catálogo inicializado con 100 productos.`

**2) Arrancar Inicio-Sesion (puerto 8082), en otra terminal:**
```bash
cd Inicio-Sesion
./mvnw spring-boot:run
```
Al iniciar siembra **5 clientes de prueba** (contraseña `ecomarket123`):
`ana.torres@ecomarket.cl`, `bruno.rojas@ecomarket.cl`, `carla.munoz@ecomarket.cl`,
`diego.perez@ecomarket.cl` (ACTIVOS) y `elena.soto@ecomarket.cl` (INACTIVO, para probar el 403).

**3) Arrancar el Carrito (puerto 8083), en otra terminal:**
```bash
cd Carrito-Compra
./mvnw spring-boot:run
```

**4) Arrancar el API Gateway (puerto 8081), en otra terminal:**
```bash
cd Api-Gateway
./mvnw spring-boot:run
```
Verifica que está arriba abriendo `http://localhost:8081/` (muestra las rutas).
A partir de aquí, todas las peticiones se hacen contra el **8081** y el gateway las
reenvía al microservicio correcto.

Alternativamente, tras `./mvnw clean package`, puedes ejecutar los JAR directamente:
```bash
java -jar target/Catalogo-Inventario-0.0.1-SNAPSHOT.jar
java -jar target/Carrito-Compra-0.0.1-SNAPSHOT.jar
java -jar target/Api-Gateway-0.0.1-SNAPSHOT.jar
```

> El Gateway no necesita MySQL (es *stateless*). Los microservicios sí.

### 5.1 Endpoints principales

**Inicio-Sesion (`http://localhost:8082` — vía gateway: `http://localhost:8081`):**

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/auth/login` | Inicia sesión (correo + contraseña) y devuelve el JWT |
| POST | `/api/v1/auth/logout` | Cierra la sesión del token |
| POST | `/api/v1/auth/validar` | Indica si un token es válido y de qué cliente es |
| PUT | `/api/v1/auth/cambiar-contrasena` | Cambia la contraseña (actual + nueva + repetir) |
| PUT | `/api/v1/auth/cambiar-correo` | Cambia el correo (contraseña + nuevo correo) |

**Catálogo (`http://localhost:8084`):**

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/productos` | Lista todos los productos |
| GET | `/api/v1/productos/{id}` | Obtiene un producto por id |
| GET | `/api/v1/productos/buscar?nombre=...` | Busca por nombre |
| GET | `/api/v1/productos/categoria/{categoria}` | Filtra por categoría |
| POST | `/api/v1/productos` | Crea un producto |
| PUT | `/api/v1/productos/{id}` | Actualiza un producto |
| DELETE | `/api/v1/productos/{id}` | Elimina un producto |
| GET | `/api/v1/inventario/{id}/disponibilidad?cantidad=N` | Verifica stock |
| POST | `/api/v1/inventario/{id}/reservar?cantidad=N` | Reserva stock |
| POST | `/api/v1/inventario/{id}/liberar?cantidad=N` | Libera stock |
| POST | `/api/v1/inventario/{id}/confirmar?cantidad=N` | Confirma consumo de stock |
| PUT | `/api/v1/inventario/{id}/stock?nuevaCantidad=N` | Ajuste manual de stock |

**Carrito (`http://localhost:8083`):**

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/carritos/{clienteId}` | Obtiene (o crea) el carrito activo |
| POST | `/api/v1/carritos/items` | Agrega un producto (body JSON) |
| DELETE | `/api/v1/carritos/{clienteId}/items/{itemId}` | Elimina una línea |
| DELETE | `/api/v1/carritos/{clienteId}` | Vacía el carrito |
| POST | `/api/v1/compras/confirmar` | Confirma la compra (genera pedido) |
| POST | `/api/v1/compras/{pedidoId}/cancelar` | Cancela un pedido |
| GET | `/api/v1/compras/historial/{clienteId}` | Historial de pedidos |

### 5.2 Flujo de prueba rápido (curl)

```bash
# Agregar producto 1 (cantidad 2) al carrito del cliente 1
curl -X POST http://localhost:8083/api/v1/carritos/items \
  -H "Content-Type: application/json" \
  -d '{"clienteId":1,"productoId":1,"cantidad":2}'

# Ver el carrito del cliente 1
curl http://localhost:8083/api/v1/carritos/1

# Confirmar la compra
curl -X POST http://localhost:8083/api/v1/compras/confirmar \
  -H "Content-Type: application/json" \
  -d '{"clienteId":1}'

# Ver el historial de pedidos
curl http://localhost:8083/api/v1/compras/historial/1
```

> Los mismos endpoints funcionan a través del **gateway** cambiando el host y puerto
> por `http://localhost:8081` (p. ej. `GET http://localhost:8081/api/v1/productos`).

### 5.3 Pruebas en Postman (vía gateway)

Con los tres servicios arriba, importa la colección en Postman:

1. **File ▸ Import** y selecciona `EcoMarket.postman_collection.json`.
2. La colección ya trae la variable `gateway = http://localhost:8081` y datos de prueba.
3. Abre la carpeta **"5. Flujo E2E"** y ejecuta las peticiones en orden: demuestra cómo,
   al agregar al carrito, el stock baja en el catálogo, y cómo al cancelar el pedido se
   restaura — todo a través del gateway.

La guía paso a paso de cada endpoint (con cuerpos y respuestas de ejemplo) está en
**[POSTMAN.md](POSTMAN.md)**.

---

## 5.4 Documentación Swagger / OpenAPI

Cada microservicio expone su documentación **interactiva** con **springdoc-openapi**.
Con el servicio levantado, abre en el navegador:

| Servicio | Swagger UI | Especificación JSON |
|---|---|---|
| Inicio-Sesion | http://localhost:8082/swagger-ui.html | http://localhost:8082/v3/api-docs |
| Carrito-Compra | http://localhost:8083/swagger-ui.html | http://localhost:8083/v3/api-docs |
| Catalogo-Inventario | http://localhost:8084/swagger-ui.html | http://localhost:8084/v3/api-docs |

Desde la UI puedes ver la descripción de cada endpoint (`@Operation`), sus parámetros,
el esquema de request/response (generado a partir de los DTOs) y **probarlos** con
*Try it out*. La documentación se genera automáticamente y se mantiene coherente con el código.

---

## 6. Cómo ejecutar las pruebas

El proyecto tiene **cuatro tipos de pruebas**:

| Tipo | Tecnología | Qué prueba | ¿Necesita BD? |
|---|---|---|---|
| 1. Unitaria de servicio | JUnit 5 + Mockito | Lógica de negocio aislada (repos y cliente mockeados) | No |
| 2. De controlador | `@WebMvcTest` + MockMvc | Rutas, códigos HTTP y JSON (solo capa web) | No |
| 3. Integración H2 | `@SpringBootTest` + H2 | Contexto completo controller→service→repo sobre H2 en memoria | H2 (automática) |
| 4. Integración MySQL | `@SpringBootTest` + MySQL | Igual que la anterior pero contra MySQL real | Sí, MySQL encendido |

### 6.1 Ejecución normal (rápida, sin MySQL)

Corre los tipos 1, 2 y 3. Las pruebas de MySQL (`@Tag("mysql")`) se **omiten** automáticamente:

```bash
cd Catalogo-Inventario
./mvnw test

cd ../Carrito-Compra
./mvnw test
```

Las pruebas de integración usan **H2 en memoria** (perfil de test, configurado en
`src/test/resources/application.properties`), así que no necesitas MySQL para esta corrida.

### 6.2 Ejecución de las pruebas de integración con MySQL (opcional)

Requieren MySQL encendido. Usan bases de datos dedicadas de prueba
(`ecomarket_catalogo_test` y `ecomarket_carrito_test`) que se crean solas y se borran
al terminar (`ddl-auto=create-drop`). Se activan con el perfil de Maven `mysql-it`:

```bash
cd Catalogo-Inventario
./mvnw test -Pmysql-it

cd ../Carrito-Compra
./mvnw test -Pmysql-it
```

### 6.3 Ejecutar una sola clase de prueba

```bash
./mvnw test -Dtest=ProductoServiceTest
```

---

## 7. Notas de diseño

- **Database per Service:** cada microservicio tiene su propia base de datos; no se
  comparten tablas. El acoplamiento es solo por API REST.
- **Snapshot de precios:** el carrito copia nombre y precio del producto al agregarlo.
  Si el catálogo cambia el precio después, los carritos y pedidos ya creados no se alteran.
- **Anti-sobreventa:** la reserva descuenta el stock de inmediato en el catálogo; si no
  hay stock suficiente, responde 409 (Conflict).
- **Tolerancia a fallos:** si el catálogo no responde, el carrito traduce el fallo a
  HTTP 503 con un mensaje claro, en lugar de un error genérico 500. Consultar el carrito
  y el historial sigue funcionando porque usa datos locales (snapshot).
- **Dinero con `BigDecimal`:** todos los importes usan `BigDecimal` (nunca `double`) para
  evitar errores de redondeo.

---

## 8. Tecnologías

`Java 21` · `Spring Boot 4.1.0` · `Spring Web MVC` · `RestClient` · `API Gateway (proxy REST)` ·
`Spring Data JPA` · `Hibernate` · `Bean Validation` · `Lombok` · `MySQL` · `H2` ·
`JUnit 5` · `Mockito` · `AssertJ` · `MockMvc` · `Maven`