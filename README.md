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
- Roles soportados: `ADMIN`, `USER_AD`, `PROD_AD`, `CLIENT` (alias legacy `USER` aceptado y mapeado a `CLIENT` en nuevas altas).
- Reglas de acceso:
  - `POST /auth/**`: público
  - `GET /productos/**`: público
  - `POST|PUT|DELETE /productos/**`: `ADMIN` o `PROD_AD`
  - `GET /users`: `ADMIN` o `USER_AD`
  - `POST /users`: `ADMIN` o `USER_AD`
  - `PUT /users/**`: `ADMIN` o `USER_AD`
  - `DELETE /users/**`: `ADMIN` o `USER_AD`
  - `PATCH /users/me`: autenticado (sin rol especial)
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

## Endpoints

### Autenticación
- `POST /auth/register`
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
- `POST /auth/login`
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

### Usuarios (personales)
- `GET /users/me` (JWT):
  - Respuesta: `UserDto`
- `PATCH /users/me` (JWT):
  - Body: `Profile` parcial o completo
  - Respuesta: `UserDto` actualizado

### Usuarios (administración)
- `GET /users` (ADMIN/USER_AD):
  - Query: `page` (def `0`), `size` (def `20`), `q` (búsqueda por email contiene)
  - Respuesta: `Page<UserDto>`
- `GET /users/{id}` (ADMIN/USER_AD):
  - Respuesta: `UserDto`
- `POST /users` (ADMIN/USER_AD):
  - Body: igual a `register`
  - Respuesta: `UserDto` creado
- `PUT /users/{id}` (ADMIN/USER_AD):
  - Body: campos de registro (actualiza `email`, `role`, `profile`)
  - Respuesta: `UserDto` actualizado
- `DELETE /users/{id}` (ADMIN/USER_AD):
  - Respuesta: `204 No Content`

### Productos
- `GET /productos` (público):
  - Respuesta: `ProductoDTO[]`
- `GET /productos/{id}` (público):
  - Respuesta: `ProductoDTO`
- `GET /productos/{id}/precio` (público):
  - Respuesta:
    ```json
    {
      "precioOriginal": 100.0,
      "precioConIva": 119.0,
      "ivaPorcentaje": 0.19,
      "ivaMonto": 19.0
    }
    ```
- `POST /productos` (ADMIN/PROD_AD, multipart/form-data):
  - Requeridos: `nombre`, `precio`, `tipo`
  - Opcionales: `descripcion`, `stock`, `imagen` (archivo)
  - Respuesta: `ProductoDTO`
- `POST /productos` (ADMIN/PROD_AD, application/json):
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
- `PUT /productos/{id}` (ADMIN/PROD_AD):
  - Body parcial:
    ```json
    { "nombre": "string", "descripcion": "string", "precio": 119.0, "tipo": "Categoria", "imagenUrl": "https://...", "stock": 5 }
    ```
  - Reglas: `precio>0`; `tipo` no vacío; `stock<0` → `0`
- `DELETE /productos/{id}` (ADMIN/PROD_AD):
  - Respuesta: `204 No Content`
- `POST /productos/{id}/imagen` (multipart/form-data):
  - Campo: `imagen` (archivo)
  - Respuesta: `{ "url": "https://..." }` o `500` si falla la subida
- `POST /productos/{id}/imagen` (application/json):
  - Body: `{ "url": "https://..." }`
  - Respuesta: `{ "url": "https://..." }`

### Imágenes
- `POST /imagenes` (multipart/form-data, público):
  - Campo: `imagen` (archivo)
  - Respuesta: `{ "url": "https://..." }`

## Ejemplos de Uso (cURL)
```bash
# Registro
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email":"user@correo.com","password":"Abc123",
    "nombre":"Juan","apellido":"Pérez","telefono":"999999",
    "direccion":"Av. Siempre Viva 742","region":"Metropolitana","ciudad":"Santiago","codigoPostal":"8320000",
    "role":"CLIENT"
  }'

# Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@correo.com","password":"Abc123"}'

# Perfil personal (GET y PATCH)
curl -H "Authorization: Bearer TOKEN" http://localhost:8080/users/me
curl -X PATCH http://localhost:8080/users/me \
  -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" \
  -d '{"telefono":"987654321","direccion":"Calle Falsa 123","region":"Valparaíso","ciudad":"Viña del Mar"}'

# Productos (público)
curl http://localhost:8080/productos
curl http://localhost:8080/productos/1
curl http://localhost:8080/productos/1/precio

# Crear producto (ADMIN/PROD_AD) JSON
curl -X POST http://localhost:8080/productos \
  -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" \
  -d '{"nombre":"Producto","descripcion":"<p>HTML</p>","precio":119,"tipo":"Categoria","stock":5}'

# Subir imagen a producto
curl -X POST http://localhost:8080/productos/1/imagen \
  -H "Authorization: Bearer TOKEN" \
  -F "imagen=@/ruta/imagen.jpg"

# Usuarios admin (ADMIN/USER_AD)
curl -H "Authorization: Bearer TOKEN" "http://localhost:8080/users?page=0&size=20&q=user"
```

## Manejo de Errores
- Global: `400` (validación), `404` (no encontrado), `422` (Bean Validation), `500` (error interno)
- Formato:
  ```json
  { "error":"bad_request", "message":"detalle", "path":"/ruta", "timestamp":"2025-01-01T00:00:00Z" }
  ```

## Ajustes de Esquema (Automáticos al Arranque)
- Remueve columna obsoleta `producto.categoria_id`.
- Actualiza constraint `users_role_check` para permitir roles (`ADMIN`,`USER_AD`,`PROD_AD`,`CLIENT`,`USER`).

## Notas
- `tipo` en JSON se mapea internamente a `categoriaNombre` en la entidad `Producto`.
- `precioOriginal` es informativo para UI (mostrar “precio menor”); el backend opera con `precio` como total con IVA.
- CORS: asegurado para `ALLOWED_ORIGINS` y métodos `GET,POST,PUT,PATCH,DELETE,OPTIONS`.

