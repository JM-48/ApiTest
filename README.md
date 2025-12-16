# API Catálogo — Spring Boot

## Resumen
- Framework: Spring Boot 3.3.x
- Persistencia: JPA/Hibernate (PostgreSQL)
- Seguridad: JWT (Bearer) con control de roles
- Documentación: Swagger UI (`/swagger-ui/index.html`) y OpenAPI (`/v3/api-docs`)
- Integración externa: Cloudinary para imágenes

## Requisitos
- Java 17+
- Maven (wrapper incluido)
- PostgreSQL (o perfil `dev` con Postgres embebido)

## Variables de Entorno
- `JWT_SECRET`: clave Base64 para firmar JWT (HS256). Si no se setea, se genera una clave efímera en cada arranque.
- `ALLOWED_ORIGINS`: orígenes CORS permitidos, separados por coma. Ej: `http://localhost:5173,https://tu-dominio`
- Base de datos en Render:
  - `JDBC_DATABASE_URL` o `DATABASE_URL`
  - `DB_USER`, `DB_PASS` si aplica
- Cloudinary:
  - `CLOUDINARY_CLOUD_NAME`
  - `CLOUDINARY_API_KEY`
  - `CLOUDINARY_API_SECRET`

## Ejecución
```bash
# Compilar
./mvnw.cmd -q -DskipTests package

# Ejecutar en default profile
./mvnw.cmd spring-boot:run

# Ejecutar en perfil dev (Postgres embebido)
./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

## Seguridad y Roles
- Roles soportados: `ADMIN`, `USER_AD`, `PROD_AD`, `VENDEDOR`, `CLIENT` (alias legacy `USER` aceptado y mapeado a `CLIENT` en nuevas altas).
- Reglas de acceso (v1):
  - `POST /api/v1/auth/**`: público
  - `GET /api/v1/productos/**`: público
  - `POST|PUT|DELETE /api/v1/productos/**`: `ADMIN` o `PROD_AD` o `VENDEDOR`
  - `GET /api/v1/users`: `ADMIN` o `USER_AD`
  - `POST /api/v1/users`: `ADMIN` o `USER_AD`
  - `PUT /api/v1/users/**`: `ADMIN` o `USER_AD`
  - `DELETE /api/v1/users/**`: `ADMIN` o `USER_AD`
  - `PATCH /api/v1/users/me`: autenticado (sin rol especial)
  - `GET|POST|PUT|DELETE /api/v1/cart/**`: `CLIENT` o `ADMIN`
  - `POST /api/v1/checkout/**`: `CLIENT` o `ADMIN`
  - `GET /api/v1/orders/admin`: `ADMIN`
- Errores:
  - `401`: `{"error":"unauthorized","message":"Token requerido"}`
  - `403`: `{"error":"forbidden","message":"Acceso denegado"}`

## Modelos y DTOs
- `ProductoDTO` (respuesta):
  ```json
  {
    "id": 1,
    "nombre": "string",
    "descripcion": "Texto (HTML/Markdown permitido)",
    "tipo": "string",
    "precio": 119.0,
    "precioOriginal": 100.0,
    "imagen": "https://...",
    "stock": 10
  }
  ```
  - `precio` se considera “precio con IVA (19%)”. `precioOriginal` = redondeo de `precio / 1.19` a 2 decimales.
  - `descripcion` se almacena en `TEXT` y acepta HTML/Markdown (sin sanitización).
  - `stock` negativo se normaliza a `0` en crear/actualizar.
- `DetalleOrdenDTO` (respuesta):
  ```json
  {
    "productoId": 1,
    "nombre": "Polera Negra",
    "precioUnitario": 119.0,
    "cantidad": 2,
    "total": 238.0,
    "imagen": "https://..."
  }
  ```
- `OrdenDTO` (respuesta):
  ```json
  {
    "id": 42,
    "status": "CART",
    "total": 238.0,
    "destinatario": "Juan Pérez",
    "direccion": "Av. Siempre Viva 742",
    "region": "Metropolitana",
    "ciudad": "Santiago",
    "codigoPostal": "8320000",
    "items": [
      {
        "productoId": 1,
        "nombre": "Polera Negra",
        "precioUnitario": 119.0,
        "cantidad": 2,
        "total": 238.0,
        "imagen": "https://..."
      }
    ]
  }
  ```
- `CompraDTO` (respuesta):
  ```json
  {
    "id": 7,
    "ordenId": 42,
    "estado": "CONFIRMADA",
    "monto": 238.0,
    "referenciaPago": "PAY-123",
    "fechaPago": "2025-12-15T19:20:30Z"
  }
  ```
- `Profile` (entrada/salida):
  ```json
  {
    "nombre": "string",
    "apellido": "string",
    "telefono": "string",
    "direccion": "string",
    "region": "string",
    "ciudad": "string",
    "codigoPostal": "string"
  }
  ```
- `UserDto` (respuesta):
  ```json
  {
    "id": 1,
    "email": "user@correo.com",
    "role": "CLIENT",
    "profile": { /* objeto Profile */ },
    "region": "string",
    "ciudad": "string"
  }
  ```

## Endpoints (v1)

### Autenticación
- `POST /api/v1/auth/register`
  - Body:
    ```json
    {
      "email": "user@correo.com",
      "password": "Abc123",
      "nombre": "Juan",
      "apellido": "Pérez",
      "telefono": "999999",
      "direccion": "Av. Siempre Viva 742",
      "region": "Metropolitana",
      "ciudad": "Santiago",
      "codigoPostal": "8320000",
      "role": "CLIENT"
    }
    ```
  - Respuesta: `UserDto`
- `POST /api/v1/auth/login`
  - Body:
    ```json
    { "email": "user@correo.com", "password": "Abc123" }
    ```
  - Respuesta:
    ```json
    {
      "token": "jwt...",
      "user": { "id": 1, "email": "user@correo.com", "role": "CLIENT", "profile": { /* ... */ } }
    }
    ```

- ### Usuarios (personales)
- `GET /api/v1/users/me` (JWT):
  - Respuesta: `UserDto`
- `PATCH /api/v1/users/me` (JWT):
  - Body: `Profile` parcial o completo
  - Respuesta: `UserDto` actualizado

- ### Usuarios (administración)
- `GET /api/v1/users` (ADMIN/USER_AD):
  - Query: `page` (def `0`), `size` (def `20`), `q` (búsqueda por email contiene)
  - Respuesta: `Page<UserDto>`
- `GET /api/v1/users/{id}` (ADMIN/USER_AD):
  - Respuesta: `UserDto`
- `POST /api/v1/users` (ADMIN/USER_AD):
  - Body: igual a `register`
  - Respuesta: `UserDto` creado
- `PUT /api/v1/users/{id}` (ADMIN/USER_AD):
  - Body: campos de registro (actualiza `email`, `role`, `profile`)
  - Respuesta: `UserDto` actualizado
- `DELETE /api/v1/users/{id}` (ADMIN/USER_AD):
  - Respuesta: `204 No Content`

- ### Productos
- `GET /api/v1/productos` (público):
  - Respuesta: `ProductoDTO[]`
- `GET /api/v1/productos/{id}` (público):
  - Respuesta: `ProductoDTO`
- `GET /api/v1/productos/{id}/precio` (público):
  - Respuesta:
    ```json
    {
      "precioOriginal": 100.0,
      "precioConIva": 119.0,
      "ivaPorcentaje": 0.19,
      "ivaMonto": 19.0
    }
    ```
- `POST /api/v1/productos` (ADMIN/PROD_AD/VENDEDOR, multipart/form-data):
  - Requeridos: `nombre`, `precio`, `tipo`
  - Opcionales: `descripcion`, `stock`, `imagen` (archivo)
  - Respuesta: `ProductoDTO`
- `POST /api/v1/productos` (ADMIN/PROD_AD/VENDEDOR, application/json):
  - Body:
    ```json
    {
      "nombre": "string",
      "descripcion": "string",
      "precio": 119.0,
      "tipo": "Categoria",
      "imagenUrl": "https://...",
      "stock": 10
    }
    ```
  - Respuesta: `ProductoDTO`
- `PUT /api/v1/productos/{id}` (ADMIN/PROD_AD/VENDEDOR):
  - Body parcial:
    ```json
    { "nombre": "string", "descripcion": "string", "precio": 119.0, "tipo": "Categoria", "imagenUrl": "https://...", "stock": 5 }
    ```
  - Reglas: `precio>0`; `tipo` no vacío; `stock<0` → `0`
- `DELETE /api/v1/productos/{id}` (ADMIN/PROD_AD/VENDEDOR):
  - Respuesta: `204 No Content`
- `POST /api/v1/productos/{id}/imagen` (multipart/form-data):
  - Campo: `imagen` (archivo)
  - Respuesta: `{ "url": "https://..." }` o `500` si falla la subida
- `POST /api/v1/productos/{id}/imagen` (application/json):
  - Body: `{ "url": "https://..." }`
  - Respuesta: `{ "url": "https://..." }`

- ### Imágenes
- `POST /api/v1/imagenes` (multipart/form-data, público):
  - Campo: `imagen` (archivo)
  - Respuesta: `{ "url": "https://..." }`

### Carrito
- `GET /api/v1/cart` (JWT):
  - Respuesta: `OrdenDTO` con `items`
- Ejemplo:
  ```json
  {
    "id": 42,
    "status": "CART",
    "total": 238.0,
    "destinatario": "Juan Pérez",
    "direccion": "Av. Siempre Viva 742",
    "region": "Metropolitana",
    "ciudad": "Santiago",
    "codigoPostal": "8320000",
    "items": [
      {
        "productoId": 1,
        "nombre": "Polera Negra",
        "precioUnitario": 119.0,
        "cantidad": 2,
        "total": 238.0,
        "imagen": "https://..."
      }
    ]
  }
  ```
- `POST /api/v1/cart/items` (JWT):
  - Body: `{ "productoId": 1, "cantidad": 2 }`
  - Respuesta: `OrdenDTO` actualizado
- `PUT /api/v1/cart/items/{productoId}` (JWT):
  - Body: `{ "cantidad": 3 }`
  - Respuesta: `OrdenDTO` actualizado
- `DELETE /api/v1/cart/items/{productoId}` (JWT):
  - Respuesta: `OrdenDTO` actualizado

### Checkout
- `POST /api/v1/checkout` (JWT, Content-Type: application/json):
  - Body:
    ```json
    {
      "items": [
        { "productoId": "1", "nombre": "Prod", "precioUnitario": 1000, "cantidad": 1 }
      ],
      "total": 1000,
      "metodoEnvio": "domicilio",
      "metodoPago": "local",
      "destinatario": "Juan Perez",
      "direccion": "Calle 123",
      "region": "Región Metropolitana de Santiago",
      "ciudad": "Santiago",
      "codigoPostal": "0000000"
    }
    ```
  - Validaciones:
    - `items`: arreglo no vacío; por ítem `cantidad >= 1`, `precioUnitario >= 0`, `nombre` requerido
    - `total`: debe coincidir con suma `precioUnitario * cantidad` (tolerancia 0.01)
    - `destinatario`, `direccion`, `region`, `ciudad`, `codigoPostal` obligatorios
    - `metodoEnvio`: `domicilio|retiro`
    - `metodoPago`: `local|tarjeta`
  - Respuestas:
    - `201`:
      ```json
      { "id":"<ordenId>", "status":"PENDING", "total":1000, "items":[...], "createdAt":"ISOString" }
      ```
    - `422`: `{ "message":"Validation error", "details":[ "...", "..." ] }`
    - `401`: `{ "message":"Unauthorized" }`
- `POST /api/v1/checkout/{ordenId}/confirm` (JWT, Content-Type: application/json):
  - Body: `{ "referenciaPago": "WEB-1734320000000" }`
  - Respuestas:
    - `200`: `{ "ordenId":"<ordenId>", "estado":"PAID", "referenciaPago":"WEB-..." }`
    - `403`: cuando la orden no pertenece al usuario
    - `404`: orden no existe
    - `409`: `{ "message":"Order already confirmed" }`
- `POST /api/v1/checkout/{ordenId}/confirm` (JWT):
  - Body: `{ "referenciaPago": "PAY-123" }`
  - Respuesta: `CompraDTO`
- Ejemplo:
  ```json
  {
    "id": 7,
    "ordenId": 42,
    "estado": "CONFIRMADA",
    "monto": 238.0,
    "referenciaPago": "PAY-123",
    "fechaPago": "2025-12-15T19:20:30Z"
  }
  ```

### Órdenes
- `GET /api/v1/orders` (JWT):
  - Respuesta: `OrdenDTO[]` del usuario
- `GET /api/v1/orders/admin` (ADMIN):
  - Respuesta: `OrdenDTO[]` de todos los usuarios
```bash
# Registro
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email":"user@correo.com","password":"Abc123",
    "nombre":"Juan","apellido":"Pérez","telefono":"999999",
    "direccion":"Av. Siempre Viva 742","region":"Metropolitana","ciudad":"Santiago","codigoPostal":"8320000",
    "role":"CLIENT"
  }'

# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@correo.com","password":"Abc123"}'

# Perfil personal (GET y PATCH)
curl -H "Authorization: Bearer TOKEN" http://localhost:8080/api/v1/users/me
curl -X PATCH http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" \
  -d '{"telefono":"987654321","direccion":"Calle Falsa 123","region":"Valparaíso","ciudad":"Viña del Mar"}'

# Productos (público)
curl http://localhost:8080/api/v1/productos
curl http://localhost:8080/api/v1/productos/1
curl http://localhost:8080/api/v1/productos/1/precio

# Crear producto (ADMIN/PROD_AD) JSON
curl -X POST http://localhost:8080/api/v1/productos \
  -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" \
  -d '{"nombre":"Producto","descripcion":"<p>HTML</p>","precio":119,"tipo":"Categoria","stock":5}'

# Subir imagen a producto
curl -X POST http://localhost:8080/api/v1/productos/1/imagen \
  -H "Authorization: Bearer TOKEN" \
  -F "imagen=@/ruta/imagen.jpg"

# Carrito
curl -H "Authorization: Bearer TOKEN" http://localhost:8080/api/v1/cart
curl -X POST http://localhost:8080/api/v1/cart/items \
  -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" \
  -d '{"productoId":1,"cantidad":2}'
curl -X PUT http://localhost:8080/api/v1/cart/items/1 \
  -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" \
  -d '{"cantidad":3}'
curl -X DELETE http://localhost:8080/api/v1/cart/items/1 \
  -H "Authorization: Bearer TOKEN"

# Checkout
curl -X POST http://localhost:8080/api/v1/checkout \
  -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" \
  -d '{"items":[{"productoId":"1","nombre":"Prod","precioUnitario":1000,"cantidad":1}],"total":1000,"metodoEnvio":"retiro","metodoPago":"local","destinatario":"Juan Perez","direccion":"Calle 123","region":"Región Metropolitana de Santiago","ciudad":"Santiago","codigoPostal":"0000000"}'
curl -X POST http://localhost:8080/api/v1/checkout/1/confirm \
  -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" \
  -d '{"referenciaPago":"PAY-123"}'

# Usuarios admin (ADMIN/USER_AD)
curl -H "Authorization: Bearer TOKEN" "http://localhost:8080/api/v1/users?page=0&size=20&q=user"
```

## Manejo de Errores
- Global: `400` (validación), `404` (no encontrado), `422` (Bean Validation), `500` (error interno)
- Formato:
  ```json
  { "error":"bad_request", "message":"detalle", "path":"/ruta", "timestamp":"2025-01-01T00:00:00Z" }
  ```

## Ajustes de Esquema (Automáticos al Arranque)
- Remueve columna obsoleta `producto.categoria_id`.
- Actualiza constraint `users_role_check` para permitir roles (`ADMIN`,`USER_AD`,`PROD_AD`,`VENDEDOR`,`CLIENT`,`USER`).

## Notas
- `tipo` en JSON se mapea internamente a `categoriaNombre` en la entidad `Producto`.
- `precioOriginal` es informativo para UI (mostrar “precio menor”); el backend opera con `precio` como total con IVA.
- CORS: asegurado para `ALLOWED_ORIGINS` y métodos `GET,POST,PUT,PATCH,DELETE,OPTIONS`.
