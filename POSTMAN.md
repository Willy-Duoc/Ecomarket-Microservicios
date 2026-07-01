# Guía de pruebas en Postman — EcoMarket Microservicios

Instructivo paso a paso para probar todos los endpoints de los dos microservicios
con **Postman**.

| Microservicio | Base URL |
|---|---|
| **Catalogo-Inventario** | `http://localhost:8081` |
| **Carrito-Compra** | `http://localhost:8082` |

---

## 1. Preparación

1. **Levanta MySQL** (XAMPP ▸ Start MySQL).
2. **Arranca primero el catálogo** y luego el carrito (en dos terminales):
   ```bash
   cd Catalogo-Inventario && ./mvnw spring-boot:run
   cd Carrito-Compra     && ./mvnw spring-boot:run
   ```
   Al arrancar el catálogo verás en el log: `Catálogo inicializado con 100 productos.`
3. Abre **Postman**.

### Configurar variables de entorno (recomendado)
En Postman: **Environments ▸ +** y crea uno llamado `EcoMarket Local` con:

| Variable | Valor inicial |
|---|---|
| `catalogo` | `http://localhost:8081` |
| `carrito` | `http://localhost:8082` |

Actívalo (esquina superior derecha). Así en las URLs puedes usar `{{catalogo}}` y `{{carrito}}`.

> En todas las peticiones con cuerpo (POST/PUT), ve a la pestaña **Body ▸ raw ▸ JSON**
> y añade el header `Content-Type: application/json` (Postman lo pone solo al elegir JSON).

---

## 2. Microservicio Catálogo — Productos

### 2.1 Listar todos los productos
- **GET** `{{catalogo}}/api/catalogo`
- **Respuesta 200:** array con los 100 productos sembrados.

### 2.2 Obtener un producto por id
- **GET** `{{catalogo}}/api/catalogo/1`
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
- **GET** `{{catalogo}}/api/catalogo/buscar?nombre=solar`
- **200:** productos cuyo nombre contiene "solar" (sin importar mayúsculas).

### 2.4 Filtrar por categoría
- **GET** `{{catalogo}}/api/catalogo/categoria/MASCOTAS`
- **200:** los 10 productos de esa categoría.
- Categorías válidas: `ALIMENTOS_ORGANICOS`, `LIMPIEZA_ECOLOGICA`, `PRODUCTOS_REUTILIZABLES`,
  `HIGIENE_PERSONAL`, `HOGAR_SOSTENIBLE`, `JARDINERIA`, `AGRICULTURA`, `MASCOTAS`,
  `RECICLAJE`, `ENERGIA_SOSTENIBLE`.

### 2.5 Crear un producto
- **POST** `{{catalogo}}/api/catalogo`
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
- **PUT** `{{catalogo}}/api/catalogo/1`
- **Body:** mismo formato que crear (con los nuevos valores).
- **200** con el producto actualizado · **404** si no existe · **409** si el nuevo SKU choca.

### 2.7 Eliminar un producto
- **DELETE** `{{catalogo}}/api/catalogo/101`
- **204 No Content** si se elimina · **404** si no existe.

---

## 3. Microservicio Catálogo — Inventario (stock)

### 3.1 Verificar disponibilidad
- **GET** `{{catalogo}}/api/inventario/1/disponibilidad?cantidad=5`
- **200:** `{ "disponible": true }`

### 3.2 Reservar stock (descuenta)
- **POST** `{{catalogo}}/api/inventario/1/reservar?cantidad=5`
- **200:** `{ "stockRestante": 95 }`
- **409:** si no hay stock suficiente · **404:** si el producto no existe.

### 3.3 Liberar stock (devuelve)
- **POST** `{{catalogo}}/api/inventario/1/liberar?cantidad=5`
- **200:** `{ "stockRestante": 100 }`

### 3.4 Confirmar stock
- **POST** `{{catalogo}}/api/inventario/1/confirmar?cantidad=5`
- **200:** `{ "confirmado": true }`

### 3.5 Ajustar stock (gerente)
- **PUT** `{{catalogo}}/api/inventario/1/stock?nuevaCantidad=200`
- **200:** `{ "stockActual": 200 }` · **409** si el valor es negativo.

---

## 4. Microservicio Carrito — Carrito

### 4.1 Ver / crear el carrito de un cliente
- **GET** `{{carrito}}/api/carrito/1`
- **200:** el carrito activo del cliente 1 (se crea vacío si no existía).

### 4.2 Agregar un producto al carrito
- **POST** `{{carrito}}/api/carrito/items`
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
- **DELETE** `{{carrito}}/api/carrito/1/items/1`  (el segundo número es el `id` del ítem)
- **200:** carrito actualizado (se libera el stock reservado) · **404** si el ítem no existe.

### 4.4 Vaciar el carrito
- **DELETE** `{{carrito}}/api/carrito/1`
- **200:** carrito vacío (libera el stock de todas las líneas).

---

## 5. Microservicio Carrito — Compras

### 5.1 Confirmar la compra
- **POST** `{{carrito}}/api/compras/confirmar`
- **Body:** `{ "clienteId": 1 }`
- **200:** el pedido generado con `estado: "CONFIRMADO"` y el `total`.
- **409:** si el carrito está vacío.

### 5.2 Cancelar un pedido
- **POST** `{{carrito}}/api/compras/1/cancelar`  (el número es el `id` del pedido)
- **200:** pedido con `estado: "CANCELADO"` (restaura el stock en el catálogo) · **404** si no existe.

### 5.3 Historial de pedidos
- **GET** `{{carrito}}/api/compras/historial/1`
- **200:** lista de pedidos del cliente 1 (más recientes primero).

---

## 6. Flujo de prueba end-to-end recomendado

Ejecuta en este orden para ver el sistema completo funcionando:

1. `GET {{catalogo}}/api/catalogo/5` → mira el stock inicial (100).
2. `POST {{carrito}}/api/carrito/items` con `{ "clienteId":1, "productoId":5, "cantidad":2 }` → 200.
3. `GET {{catalogo}}/api/catalogo/5` → el stock ahora es **98** (se reservó al agregar).
4. `GET {{carrito}}/api/carrito/1` → ves el ítem y el total.
5. `POST {{carrito}}/api/compras/confirmar` con `{ "clienteId":1 }` → 200, pedido CONFIRMADO. Anota el `id` del pedido.
6. `GET {{carrito}}/api/compras/historial/1` → aparece el pedido.
7. `POST {{carrito}}/api/compras/{idPedido}/cancelar` → 200, pedido CANCELADO.
8. `GET {{catalogo}}/api/catalogo/5` → el stock vuelve a **100** (se restauró).

**Prueba de resiliencia:** apaga el catálogo (Ctrl+C en su terminal) y repite el paso 2.
Debes recibir **503 Service Unavailable** con un mensaje claro, no un error 500.

---

## 7. Resumen de códigos HTTP

| Código | Significado en este sistema |
|---|---|
| 200 | OK (operación con respuesta) |
| 201 | Creado (producto nuevo) |
| 204 | OK sin contenido (eliminación) |
| 400 | Datos inválidos (validación) — revisa `detallesValidacion` |
| 404 | Recurso no encontrado |
| 409 | Conflicto (SKU duplicado, sin stock, carrito vacío) |
| 503 | El catálogo no está disponible (solo en el carrito) |
