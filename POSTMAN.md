# Guía de pruebas en Postman — EcoMarket Microservicios

Instructivo paso a paso para probar todos los endpoints con **Postman**.
Lo recomendado es hacer TODO a través del **API Gateway** (`http://localhost:8081`),
que enruta a cada microservicio. También puedes importar directamente la colección
`EcoMarket.postman_collection.json` (File ▸ Import) que ya trae todo configurado.

| Servicio | Base URL directa | Vía gateway |
|---|---|---|
| **Api-Gateway** | `http://localhost:8081` | — |
| **Inicio-Sesion** | `http://localhost:8082` | `{{gateway}}/api/v1/auth` |
| **Carrito-Compra** | `http://localhost:8083` | `{{gateway}}/api/v1/carritos` y `/api/v1/compras` |
| **Catalogo-Inventario** | `http://localhost:8084` | `{{gateway}}/api/v1/productos` y `/api/v1/inventario` |

---

## 1. Preparación

1. **Levanta MySQL** (XAMPP ▸ Start MySQL).
2. **Arranca los 4 servicios** (una terminal cada uno, en este orden):
   ```bash
   cd Catalogo-Inventario && ./mvnw spring-boot:run   # 8084 (siembra 100 productos)
   cd Inicio-Sesion       && ./mvnw spring-boot:run   # 8082 (siembra 5 clientes)
   cd Carrito-Compra      && ./mvnw spring-boot:run   # 8083
   cd Api-Gateway         && ./mvnw spring-boot:run   # 8081
   ```
3. Abre **Postman**.

### Clientes de prueba (seed de Inicio-Sesion)
Todos con contraseña **`ecomarket123`**:

| Correo | Estado |
|---|---|
| ana.torres@ecomarket.cl | ACTIVO |
| bruno.rojas@ecomarket.cl | ACTIVO |
| carla.munoz@ecomarket.cl | ACTIVO |
| diego.perez@ecomarket.cl | ACTIVO |
| elena.soto@ecomarket.cl | INACTIVO (sirve para probar el 403) |

### Configurar variables de entorno (recomendado)
En Postman: **Environments ▸ +** y crea uno llamado `EcoMarket Local` con:

| Variable | Valor inicial |
|---|---|
| `gateway` | `http://localhost:8081` |
| `auth` | `http://localhost:8082` |
| `carrito` | `http://localhost:8083` |
| `catalogo` | `http://localhost:8084` |

Actívalo (esquina superior derecha). Así en las URLs puedes usar `{{catalogo}}` y `{{carrito}}`.

> En todas las peticiones con cuerpo (POST/PUT), ve a la pestaña **Body ▸ raw ▸ JSON**
> y añade el header `Content-Type: application/json` (Postman lo pone solo al elegir JSON).

---

## 2. Microservicio Inicio-Sesion — Autenticación (JWT)

### 2.1 Iniciar sesión
- **POST** `{{gateway}}/api/v1/auth/login`
- **Body:** `{ "correo": "ana.torres@ecomarket.cl", "contrasena": "ecomarket123" }`
- **200:** devuelve `token` (JWT), `clienteId`, nombre y expiración. **Guarda el token.**
- **401:** correo o contraseña incorrectos · **403:** cuenta INACTIVA (prueba con elena.soto).

### 2.2 Validar token
- **POST** `{{gateway}}/api/v1/auth/validar`
- **Body:** `{ "token": "<el JWT del login>" }`
- **200:** `{ "valido": true, "clienteId": 1, "correo": "..." }` (o `valido: false`).

### 2.3 Cambiar contraseña
- **PUT** `{{gateway}}/api/v1/auth/cambiar-contrasena`
- **Body:** `{ "clienteId": 1, "contrasenaActual": "ecomarket123", "nuevaContrasena": "nuevaClave1", "repetirContrasena": "nuevaClave1" }`
- **200** actualizada · **401** actual incorrecta · **400** si la repetición no coincide.

### 2.4 Cambiar correo
- **PUT** `{{gateway}}/api/v1/auth/cambiar-correo`
- **Body:** `{ "clienteId": 1, "contrasena": "ecomarket123", "nuevoCorreo": "ana.nueva@ecomarket.cl" }`
- **200** actualizado · **401** contraseña incorrecta · **409** correo ya en uso.

### 2.5 Cerrar sesión
- **POST** `{{gateway}}/api/v1/auth/logout`
- **Body:** `{ "token": "<el JWT>" }`
- **200:** la sesión se cierra; si validas ese token de nuevo, `valido: false`.

---

## 3. Microservicio Catálogo — Productos

### 2.1 Listar todos los productos
- **GET** `{{catalogo}}/api/v1/productos`
- **Respuesta 200:** array con los 100 productos sembrados.

### 2.2 Obtener un producto por id
- **GET** `{{catalogo}}/api/v1/productos/1`
- **200:**
  ```json
  {
    "id": 1, "sku": "ALI-01", "nombre": "Quinoa Orgánica",
    "categoria": "ALIMENTOS_ORGANICOS", "precio": 1990.00, "stock": 100,
    "estado": "DISPONIBLE", "fechaCreacion": "2026-06-30T12:00:00"
  }
  ```
- **404:** si el id no existe.

### 2.3 Buscar por nombre
- **GET** `{{catalogo}}/api/v1/productos/buscar?nombre=solar`
- **200:** productos cuyo nombre contiene "solar" (sin importar mayúsculas).

### 2.4 Filtrar por categoría
- **GET** `{{catalogo}}/api/v1/productos/categoria/MASCOTAS`
- **200:** los 10 productos de esa categoría.
- Categorías válidas: `ALIMENTOS_ORGANICOS`, `LIMPIEZA_ECOLOGICA`, `PRODUCTOS_REUTILIZABLES`,
  `HIGIENE_PERSONAL`, `HOGAR_SOSTENIBLE`, `JARDINERIA`, `AGRICULTURA`, `MASCOTAS`,
  `RECICLAJE`, `ENERGIA_SOSTENIBLE`.

### 2.5 Crear un producto
- **POST** `{{catalogo}}/api/v1/productos`
- **Body (raw JSON):**
  ```json
  {
    "sku": "ALI-99",
    "nombre": "Té Matcha Orgánico",
    "descripcion": "Lata 100g",
    "categoria": "ALIMENTOS_ORGANICOS",
    "precio": 5990.00,
    "stock": 40,
    "imagenUrl": "https://ecomarket.cl/img/ali-99.jpg"
  }
  ```
- **201 Created:** devuelve el producto con su `id`.
- **400:** si faltan campos o el precio es ≤ 0 (verás `detallesValidacion`).
- **409:** si el `sku` ya existe.

### 2.6 Actualizar un producto
- **PUT** `{{catalogo}}/api/v1/productos/1`
- **Body:** mismo formato que crear (con los nuevos valores).
- **200** con el producto actualizado · **404** si no existe · **409** si el nuevo SKU choca.

### 2.7 Eliminar un producto
- **DELETE** `{{catalogo}}/api/v1/productos/101`
- **204 No Content** si se elimina · **404** si no existe.

---

## 4. Microservicio Catálogo — Inventario (stock)

### 3.1 Verificar disponibilidad
- **GET** `{{catalogo}}/api/v1/inventario/1/disponibilidad?cantidad=5`
- **200:** `{ "disponible": true }`

### 3.2 Reservar stock (descuenta)
- **POST** `{{catalogo}}/api/v1/inventario/1/reservar?cantidad=5`
- **200:** `{ "stockRestante": 95 }`
- **409:** si no hay stock suficiente · **404:** si el producto no existe.

### 3.3 Liberar stock (devuelve)
- **POST** `{{catalogo}}/api/v1/inventario/1/liberar?cantidad=5`
- **200:** `{ "stockRestante": 100 }`

### 3.4 Confirmar stock
- **POST** `{{catalogo}}/api/v1/inventario/1/confirmar?cantidad=5`
- **200:** `{ "confirmado": true }`

### 3.5 Ajustar stock (gerente)
- **PUT** `{{catalogo}}/api/v1/inventario/1/stock?nuevaCantidad=200`
- **200:** `{ "stockActual": 200 }` · **409** si el valor es negativo.

---

## 5. Microservicio Carrito — Carrito

### 4.1 Ver / crear el carrito de un cliente
- **GET** `{{carrito}}/api/v1/carritos/1`
- **200:** el carrito activo del cliente 1 (se crea vacío si no existía).

### 4.2 Agregar un producto al carrito
- **POST** `{{carrito}}/api/v1/carritos/items`
- **Body:**
  ```json
  { "clienteId": 1, "productoId": 5, "cantidad": 2 }
  ```
- **200:**
  ```json
  {
    "id": 1, "clienteId": 1, "activo": true,
    "items": [
      { "id": 1, "productoId": 5, "nombreProducto": "Arroz Integral Orgánico",
        "precioUnitario": 2590.00, "cantidad": 2, "subtotal": 5180.00 }
    ],
    "total": 5180.00
  }
  ```
- **400:** si `cantidad` es 0 o falta un campo.
- **404:** si el producto no existe en el catálogo.
- **409:** si no hay stock suficiente.
- **503:** si el catálogo está apagado (¡pruébalo apagándolo!).

### 4.3 Eliminar un ítem del carrito
- **DELETE** `{{carrito}}/api/v1/carritos/1/items/1`  (el segundo número es el `id` del ítem)
- **200:** carrito actualizado (se libera el stock reservado) · **404** si el ítem no existe.

### 4.4 Vaciar el carrito
- **DELETE** `{{carrito}}/api/v1/carritos/1`
- **200:** carrito vacío (libera el stock de todas las líneas).

---

## 6. Microservicio Carrito — Compras

### 6.1 Confirmar la compra
- **POST** `{{gateway}}/api/v1/compras/confirmar`
- **Body:** `{ "clienteId": 1 }`
- **200:** el pedido generado con `estado: "CONFIRMADO"` y el `total`.
- **409:** si el carrito está vacío.
- ⚠️ **Regla de negocio:** los productos comprados **se eliminan del registro del
  Catálogo-Inventario** (si luego los consultas por id → 404).

### 5.2 Cancelar un pedido
- **POST** `{{carrito}}/api/v1/compras/1/cancelar`  (el número es el `id` del pedido)
- **200:** pedido con `estado: "CANCELADO"` (restaura el stock en el catálogo) · **404** si no existe.

### 5.3 Historial de pedidos
- **GET** `{{carrito}}/api/v1/compras/historial/1`
- **200:** lista de pedidos del cliente 1 (más recientes primero).

---

## 7. Flujo de prueba end-to-end recomendado

Ejecuta en este orden para ver el sistema completo funcionando:

1. `POST {{gateway}}/api/v1/auth/login` con Ana → 200, obtienes el token JWT.
2. `GET {{gateway}}/api/v1/productos/5` → mira el stock inicial (100).
3. `POST {{gateway}}/api/v1/carritos/items` con `{ "clienteId":1, "productoId":5, "cantidad":2 }` → 200.
4. `GET {{gateway}}/api/v1/productos/5` → el stock ahora es **98** (se reservó al agregar).
5. `GET {{gateway}}/api/v1/carritos/1` → ves el ítem y el total.
6. `POST {{gateway}}/api/v1/compras/confirmar` con `{ "clienteId":1 }` → 200, pedido CONFIRMADO.
7. `GET {{gateway}}/api/v1/productos/5` → **404**: el producto comprado fue **eliminado del catálogo** (regla de negocio).
8. `GET {{gateway}}/api/v1/compras/historial/1` → aparece el pedido (con el snapshot del producto).
9. `POST {{gateway}}/api/v1/auth/logout` con el token → 200, sesión cerrada.

**Prueba de resiliencia:** apaga el catálogo (Ctrl+C en su terminal) y repite el paso 2.
Debes recibir **503 Service Unavailable** con un mensaje claro, no un error 500.

---

## 8. Resumen de códigos HTTP

| Código | Significado en este sistema |
|---|---|
| 200 | OK (operación con respuesta) |
| 201 | Creado (producto nuevo) |
| 204 | OK sin contenido (eliminación) |
| 400 | Datos inválidos (validación) — revisa `detallesValidacion` |
| 404 | Recurso no encontrado |
| 401 | No autorizado (credenciales o token inválidos — Inicio-Sesion) |
| 403 | Prohibido (cuenta INACTIVA — Inicio-Sesion) |
| 409 | Conflicto (SKU duplicado, sin stock, carrito vacío, correo en uso) |
| 503 | El catálogo no está disponible (solo en el carrito) |
