# EcoMarket – Microservicios

Plataforma de e-commerce de productos ecológicos y sostenibles, construida con una
arquitectura de **microservicios** sobre Spring Boot. Este repositorio contiene dos
servicios independientes que se comunican por REST:

| Microservicio | Puerto | Responsabilidad | Base de datos |
|---|---|---|---|
| **Catalogo-Inventario** | 8081 | Catálogo de productos y gestión de stock | `ecomarket_catalogo` |
| **Carrito-Compra** | 8082 | Carrito de compras, pedidos e historial | `ecomarket_carrito` |

Cada servicio es **dueño exclusivo de sus datos** y tiene su propia base de datos
(patrón *Database per Service*). El Carrito nunca accede a la base del Catálogo:
le consulta por REST y guarda una copia histórica (*snapshot*) del nombre y precio
de cada producto en el momento de agregarlo. Así el carrito y el historial siguen
funcionando aunque el catálogo esté caído.

---

## 1. Arquitectura del proyecto

### 1.1 Comunicación entre servicios

```
                         HTTP / REST (RestClient)
   ┌─────────────────────┐   GET  /api/catalogo/{id}    ┌──────────────────────────┐
   │                     │ ───────────────────────────► │                          │
   │   Carrito-Compra    │   POST /api/inventario/.../  │   Catalogo-Inventario    │
   │   (puerto 8082)     │        reservar | liberar |  │   (puerto 8081)          │
   │                     │ ◄─────────────────────────── │                          │
   └─────────┬───────────┘        confirmar             └────────────┬─────────────┘
             │                                                        │
     ┌───────▼────────┐                                      ┌────────▼─────────┐
     │ MySQL          │                                      │ MySQL            │
     │ ecomarket_     │                                      │ ecomarket_       │
     │ carrito        │                                      │ catalogo         │
     └────────────────┘                                      └──────────────────┘
```

El Catálogo no conoce al Carrito (no depende de él). El Carrito sí depende del
Catálogo, pero a través de una interfaz (`CatalogoClient`) que aísla el dominio de
los detalles de HTTP. Si el catálogo no responde, el Carrito devuelve **503**
(servicio no disponible) en vez de un error genérico.

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
│
├── Catalogo-Inventario/                 # Microservicio 1 (puerto 8081)
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
│       │   │   │   ├── ProductoController.java          # /api/catalogo
│       │   │   │   └── InventarioController.java        # /api/inventario
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
│       │       └── application.properties               # Config MySQL, puerto 8081
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
└── Carrito-Compra/                      # Microservicio 2 (puerto 8082)
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
        │   │   │   ├── CarritoController.java            # /api/carrito
        │   │   │   └── CompraController.java             # /api/compras
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
        │       └── application.properties                # Config MySQL, puerto 8082, catalogo.base-url
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

El orden importa: **primero el Catálogo**, luego el Carrito (el Carrito le consulta al arrancar el flujo de compra).

**1) Arrancar el Catálogo (puerto 8081):**
```bash
cd Catalogo-Inventario
./mvnw spring-boot:run
```
Al iniciar, siembra automáticamente **100 productos** (10 categorías × 10 productos).
Verás en consola: `Catálogo inicializado con 100 productos.`

**2) Arrancar el Carrito (puerto 8082), en otra terminal:**
```bash
cd Carrito-Compra
./mvnw spring-boot:run
```

Alternativamente, tras `./mvnw clean package`, puedes ejecutar el JAR directamente:
```bash
java -jar target/Catalogo-Inventario-0.0.1-SNAPSHOT.jar
java -jar target/Carrito-Compra-0.0.1-SNAPSHOT.jar
```

### 5.1 Endpoints principales

**Catálogo (`http://localhost:8081`):**

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/catalogo` | Lista todos los productos |
| GET | `/api/catalogo/{id}` | Obtiene un producto por id |
| GET | `/api/catalogo/buscar?nombre=...` | Busca por nombre |
| GET | `/api/catalogo/categoria/{categoria}` | Filtra por categoría |
| POST | `/api/catalogo` | Crea un producto |
| PUT | `/api/catalogo/{id}` | Actualiza un producto |
| DELETE | `/api/catalogo/{id}` | Elimina un producto |
| GET | `/api/inventario/{id}/disponibilidad?cantidad=N` | Verifica stock |
| POST | `/api/inventario/{id}/reservar?cantidad=N` | Reserva stock |
| POST | `/api/inventario/{id}/liberar?cantidad=N` | Libera stock |
| POST | `/api/inventario/{id}/confirmar?cantidad=N` | Confirma consumo de stock |
| PUT | `/api/inventario/{id}/stock?nuevaCantidad=N` | Ajuste manual de stock |

**Carrito (`http://localhost:8082`):**

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/carrito/{clienteId}` | Obtiene (o crea) el carrito activo |
| POST | `/api/carrito/items` | Agrega un producto (body JSON) |
| DELETE | `/api/carrito/{clienteId}/items/{itemId}` | Elimina una línea |
| DELETE | `/api/carrito/{clienteId}` | Vacía el carrito |
| POST | `/api/compras/confirmar` | Confirma la compra (genera pedido) |
| POST | `/api/compras/{pedidoId}/cancelar` | Cancela un pedido |
| GET | `/api/compras/historial/{clienteId}` | Historial de pedidos |

### 5.2 Flujo de prueba rápido (curl)

```bash
# Agregar producto 1 (cantidad 2) al carrito del cliente 1
curl -X POST http://localhost:8082/api/carrito/items \
  -H "Content-Type: application/json" \
  -d '{"clienteId":1,"productoId":1,"cantidad":2}'

# Ver el carrito del cliente 1
curl http://localhost:8082/api/carrito/1

# Confirmar la compra
curl -X POST http://localhost:8082/api/compras/confirmar \
  -H "Content-Type: application/json" \
  -d '{"clienteId":1}'

# Ver el historial de pedidos
curl http://localhost:8082/api/compras/historial/1
```

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

`Java 21` · `Spring Boot 4.1.0` · `Spring Web MVC` · `Spring Data JPA` · `Hibernate` ·
`Bean Validation` · `Lombok` · `MySQL` · `H2` · `JUnit 5` · `Mockito` · `AssertJ` · `Maven`