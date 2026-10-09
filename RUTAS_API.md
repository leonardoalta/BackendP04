# Guía de rutas de los controllers

Este documento describe las 16 rutas implementadas en `src/main/java/com/reportesciudadanos/controller`, sus datos de entrada y sus respuestas. Los ejemplos utilizan datos ficticios; los identificadores y las fechas reales dependen de la base de datos.

## Cómo enviar solicitudes

La dirección local habitual es `http://localhost:8080`. Todas las rutas de los controllers comienzan con `/api/v1`.

- **GET:** consulta información. Estas rutas no necesitan un cuerpo JSON.
- **POST:** registra un recurso o inicia sesión. Envía el cuerpo como JSON.
- **PATCH:** modifica el estado de un reporte. Envía el cuerpo como JSON.
- **Request:** DTO que define los campos recibidos por el backend.
- **Response:** DTO que define los campos devueltos por el backend.
- **`{id}`:** identificador que debes sustituir en la URL; por ejemplo, `/reportes/12`.
- **Query parameters:** parámetros después de `?`; por ejemplo, `?page=0&size=20`.

Para los cuerpos JSON, utiliza:

```http
Content-Type: application/json
```

Excepto registro y login, todas las rutas de los controllers requieren el token obtenido al autenticarte:

```http
Authorization: Bearer TU_ACCESS_TOKEN
```

Los campos desconocidos en los cuerpos JSON se rechazan con `400`. No envíes campos que no estén definidos en el Request.

## Resumen

| Controller | Método | Ruta | Para qué sirve |
| --- | --- | --- | --- |
| AuthController | POST | `/api/v1/auth/register` | Registrar un ciudadano y obtener un token |
| AuthController | POST | `/api/v1/auth/login` | Iniciar sesión y obtener un token |
| ReporteController | POST | `/api/v1/reportes` | Crear un reporte propio |
| ReporteController | GET | `/api/v1/reportes` | Listar reportes con paginación y filtros |
| ReporteController | GET | `/api/v1/reportes/{id}` | Consultar el detalle completo de un reporte |
| ReporteController | GET | `/api/v1/reportes/mis-reportes` | Listar únicamente los reportes propios |
| ReporteController | PATCH | `/api/v1/reportes/{id}/estado` | Cambiar estado y crear un seguimiento |
| CategoriaController | GET | `/api/v1/categorias` | Consultar categorías activas |
| CategoriaController | GET | `/api/v1/categorias/{id}` | Consultar una categoría activa |
| ZonaController | GET | `/api/v1/zonas` | Consultar zonas activas |
| ZonaController | GET | `/api/v1/zonas/{id}` | Consultar una zona activa |
| EvidenciaController | GET | `/api/v1/reportes/{id}/evidencias` | Consultar evidencias de un reporte |
| EvidenciaController | POST | `/api/v1/reportes/{id}/evidencias` | Registrar una evidencia ficticia |
| SeguimientoController | GET | `/api/v1/reportes/{id}/seguimientos` | Consultar el historial de cambios de estado |
| EventoContextoController | POST | `/api/v1/eventos-contexto` | Registrar un evento ambiental simulado |
| EventoContextoController | GET | `/api/v1/reportes/{id}/eventos-contexto` | Consultar eventos de un reporte |

## 1. AuthController: autenticación

### POST `/api/v1/auth/register`

**Propósito:** crear un usuario con rol `CIUDADANO`. También devuelve un JWT para utilizar inmediatamente las rutas protegidas.

**Acceso:** público; no requiere token. No permite elegir un rol privilegiado.

**Recibe:** `RegisterRequest`.

| Campo | Obligatorio | Validación |
| --- | --- | --- |
| `nombre` | Sí | No vacío; máximo 150 caracteres |
| `correoElectronico` | Sí | Correo válido; máximo 254 caracteres; único |
| `password` | Sí | Entre 12 y 72 caracteres; máximo 72 bytes UTF-8 |

```json
{
  "nombre": "Ciudadano de prueba",
  "correoElectronico": "ciudadano@example.test",
  "password": "EjemploSeguro123!"
}
```

**Responde:** `201 Created`, con `AuthResponse`:

```json
{
  "accessToken": "JWT_GENERADO_POR_EL_SERVIDOR",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

`expiresIn` indica la duración del token en segundos; por defecto es 3600 y se puede configurar. La respuesta no incluye la contraseña ni su hash. El correo se normaliza a minúsculas y la contraseña se guarda mediante BCrypt.

**Errores propios:** `400` por datos inválidos o contraseña de más de 72 bytes; `409` si el correo ya existe.

### POST `/api/v1/auth/login`

**Propósito:** verificar las credenciales de un usuario existente y emitir un JWT.

**Acceso:** público; no requiere token.

**Recibe:** `LoginRequest`.

| Campo | Obligatorio | Validación |
| --- | --- | --- |
| `correoElectronico` | Sí | Correo válido, no vacío |
| `password` | Sí | No vacío; máximo 72 caracteres |

```json
{
  "correoElectronico": "ciudadano@example.test",
  "password": "EjemploSeguro123!"
}
```

**Responde:** `200 OK`, con el mismo formato `AuthResponse` del registro: `accessToken`, `tokenType` y `expiresIn`.

**Errores propios:** `400` por entrada inválida; `401` si las credenciales son incorrectas o el usuario está deshabilitado.

## 2. ReporteController: reportes ciudadanos

### POST `/api/v1/reportes`

**Propósito:** registrar una incidencia y asociarla al ciudadano autenticado.

**Acceso:** únicamente `CIUDADANO`.

**Recibe:** `ReporteCreateRequest`.

| Campo | Obligatorio | Validación |
| --- | --- | --- |
| `categoriaId` | Sí | Número positivo; categoría existente y activa |
| `zonaId` | Sí | Número positivo; zona existente y activa |
| `titulo` | Sí | No vacío; máximo 200 caracteres |
| `descripcion` | Sí | No vacía; máximo 4000 caracteres |
| `prioridad` | Sí | `BAJA`, `MEDIA` o `ALTA` |

```json
{
  "categoriaId": 1,
  "zonaId": 1,
  "titulo": "Alumbrado dañado",
  "descripcion": "Una lámpara de la calle permanece apagada",
  "prioridad": "MEDIA"
}
```

El servidor asigna el propietario a partir del token, el estado inicial `REGISTRADO` y las fechas. No debes enviar `usuarioId`, `propietario`, `estado` ni fechas.

**Responde:** `201 Created`, con `ReporteResponse` y cabecera `Location: /api/v1/reportes/12` para el ejemplo:

```json
{
  "id": 12,
  "categoriaId": 1,
  "zonaId": 1,
  "titulo": "Alumbrado dañado",
  "descripcion": "Una lámpara de la calle permanece apagada",
  "estado": "REGISTRADO",
  "prioridad": "MEDIA",
  "fechaCreacion": "2026-10-08T10:30:00",
  "fechaActualizacion": "2026-10-08T10:30:00"
}
```

**Errores propios:** `400` por campos inválidos; `403` por rol no autorizado; `404` si la categoría o zona no existe; `409` si está inactiva.

### GET `/api/v1/reportes`

**Propósito:** consultar un listado resumido, paginado y filtrado.

**Acceso:** los tres roles. Un ciudadano ve exclusivamente sus reportes; un operador o responsable puede consultar todos.

**Recibe:** parámetros opcionales en la URL; no recibe cuerpo JSON.

| Parámetro | Por defecto | Uso y valores |
| --- | --- | --- |
| `page` | `0` | Página a consultar, comenzando en cero; no negativa |
| `size` | `20` | Elementos por página; entre 1 y 100 |
| `sort` | `fechaCreacion` | Campo de orden: `id`, `fechaCreacion`, `prioridad` o `estado` |
| `estado` | Sin filtro | `REGISTRADO`, `EN_REVISION`, `EN_ATENCION`, `RESUELTO` o `CERRADO` |
| `prioridad` | Sin filtro | `BAJA`, `MEDIA` o `ALTA` |
| `categoriaId` | Sin filtro | Identificador de categoría |
| `zonaId` | Sin filtro | Identificador de zona |

```http
GET /api/v1/reportes?page=0&size=10&sort=fechaCreacion&estado=REGISTRADO&prioridad=MEDIA&categoriaId=1&zonaId=1
```

Los filtros se combinan: un reporte debe cumplir todos los filtros enviados. El orden es descendente y utiliza `id` como criterio adicional. Ordenar por `prioridad` o `estado` ordena los valores almacenados, sin un orden especial por gravedad o avance. Un identificador de filtro que no coincide con registros produce un listado vacío.

**Responde:** `200 OK`, con `PageResponse<ReporteSummaryResponse>`:

```json
{
  "content": [
    {
      "id": 12,
      "titulo": "Alumbrado dañado",
      "estado": "REGISTRADO",
      "prioridad": "MEDIA",
      "fechaCreacion": "2026-10-08T10:30:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

`content` contiene únicamente la página solicitada. `totalElements` cuenta todos los reportes que coinciden con los filtros y permisos; `totalPages` indica cuántas páginas hay. No se incluyen descripción, evidencias, seguimientos ni eventos. Sin resultados, `content` es `[]`, `totalElements` es `0` y `totalPages` es `0`.

**Errores propios:** `400` por paginación, ordenamiento o valores de filtros inválidos.

### GET `/api/v1/reportes/{id}`

**Propósito:** consultar todos los datos disponibles de un reporte concreto.

**Acceso:** ciudadano propietario, `OPERADOR` o `RESPONSABLE_ATENCION`.

**Recibe:** `id` del reporte en la URL. Ejemplo: `/api/v1/reportes/12`. Sin cuerpo JSON.

**Responde:** `200 OK`, con `ReporteDetailResponse`:

```json
{
  "reporte": {
    "id": 12,
    "categoriaId": 1,
    "zonaId": 1,
    "titulo": "Alumbrado dañado",
    "descripcion": "Una lámpara de la calle permanece apagada",
    "estado": "REGISTRADO",
    "prioridad": "MEDIA",
    "fechaCreacion": "2026-10-08T10:30:00",
    "fechaActualizacion": "2026-10-08T10:30:00"
  },
  "evidencias": [],
  "seguimientos": [],
  "eventosContexto": []
}
```

`reporte` tiene el formato `ReporteResponse`. Las tres colecciones contienen los mismos objetos documentados en las rutas de evidencias, seguimientos y eventos; si no hay registros son listas vacías.

**Errores propios:** `403` si un ciudadano intenta consultar el reporte de otro; `404` si el reporte no existe.

### GET `/api/v1/reportes/mis-reportes`

**Propósito:** consultar exclusivamente los reportes del usuario autenticado.

**Acceso:** los tres roles; el filtro de propietario se aplica también al personal. Como la creación de reportes está limitada a ciudadanos, un usuario de personal puede obtener una lista vacía.

**Recibe:** `page` y `size`, con valores por defecto `0` y `20`, respectivamente. Página no negativa y tamaño entre 1 y 100. No recibe cuerpo JSON.

```http
GET /api/v1/reportes/mis-reportes?page=0&size=20
```

**Responde:** `200 OK`, con `PageResponse<ReporteSummaryResponse>`, el mismo formato del listado general. Ordena por `fechaCreacion` descendente y después por `id` descendente. Esta ruta no implementa filtros de estado, prioridad, categoría o zona, ni orden personalizado.

**Errores propios:** `400` por paginación inválida.

### PATCH `/api/v1/reportes/{id}/estado`

**Propósito:** cambiar el estado del reporte y guardar automáticamente un seguimiento del cambio.

**Acceso:** `OPERADOR` o `RESPONSABLE_ATENCION`, según la transición. Los ciudadanos no pueden cambiar estados.

**Recibe:** `id` en la URL y `ReporteStatusRequest` en el cuerpo.

| Campo | Obligatorio | Validación |
| --- | --- | --- |
| `estado` | Sí | Estado de destino válido y transición permitida |
| `comentario` | Sí | No vacío; máximo 2000 caracteres |

```json
{
  "estado": "EN_REVISION",
  "comentario": "Se revisará la información del reporte"
}
```

La distribución actual de permisos es:

| Rol | Estado anterior | Estado nuevo permitido |
| --- | --- | --- |
| OPERADOR | REGISTRADO | EN_REVISION |
| OPERADOR | EN_REVISION | REGISTRADO o EN_ATENCION |
| RESPONSABLE_ATENCION | EN_ATENCION | EN_REVISION o RESUELTO |
| RESPONSABLE_ATENCION | RESUELTO | EN_ATENCION o CERRADO |

Desde `CERRADO` no se permiten cambios. Tampoco se admite cambiar al mismo estado. Esta matriz corresponde a la política implementada actualmente.

**Responde:** `200 OK`, con `ReporteResponse`, el mismo formato de la creación pero con `estado` y `fechaActualizacion` actualizados. Por ejemplo, después del cambio anterior `estado` será `EN_REVISION`.

El seguimiento registra estado anterior, estado nuevo, comentario, rol del actor y fecha. El cambio de estado y su seguimiento se guardan en la misma transacción.

**Errores propios:** `400` por datos inválidos; `403` por rol no autorizado o transición reservada al otro rol; `404` si el reporte no existe; `409` por transición no permitida o conflicto de actualización concurrente.

## 3. CategoriaController: catálogo de categorías

### GET `/api/v1/categorias`

**Propósito:** obtener las categorías disponibles para crear reportes.

**Acceso:** cualquiera de los tres roles autenticados.

**Recibe:** no requiere parámetros ni cuerpo JSON.

**Responde:** `200 OK`, con una lista de `CategoriaResponse`, ordenada por nombre ascendente:

```json
[
  {
    "id": 1,
    "nombre": "Alumbrado",
    "descripcion": "Incidencias de iluminación pública"
  }
]
```

Solo incluye categorías activas. Si no hay categorías activas, devuelve `[]`. No está paginado.

### GET `/api/v1/categorias/{id}`

**Propósito:** consultar una categoría activa mediante su identificador.

**Acceso:** cualquiera de los tres roles autenticados.

**Recibe:** `id` en la URL. Sin cuerpo JSON.

**Responde:** `200 OK`, con un objeto `CategoriaResponse`:

```json
{
  "id": 1,
  "nombre": "Alumbrado",
  "descripcion": "Incidencias de iluminación pública"
}
```

**Errores propios:** `404` si no existe; `409` si la categoría está inactiva.

## 4. ZonaController: catálogo de zonas

### GET `/api/v1/zonas`

**Propósito:** obtener las zonas disponibles para ubicar un reporte.

**Acceso:** cualquiera de los tres roles autenticados.

**Recibe:** no requiere parámetros ni cuerpo JSON.

**Responde:** `200 OK`, con una lista de `ZonaResponse`, ordenada por nombre ascendente:

```json
[
  {
    "id": 1,
    "nombre": "Centro",
    "referencia": "Zona central"
  }
]
```

Solo incluye zonas activas. Si no hay zonas activas, devuelve `[]`. No está paginado.

### GET `/api/v1/zonas/{id}`

**Propósito:** consultar una zona activa mediante su identificador.

**Acceso:** cualquiera de los tres roles autenticados.

**Recibe:** `id` en la URL. Sin cuerpo JSON.

**Responde:** `200 OK`, con un objeto `ZonaResponse`:

```json
{
  "id": 1,
  "nombre": "Centro",
  "referencia": "Zona central"
}
```

**Errores propios:** `404` si no existe; `409` si la zona está inactiva.

## 5. EvidenciaController: evidencias ficticias

### GET `/api/v1/reportes/{id}/evidencias`

**Propósito:** consultar las evidencias asociadas a un reporte.

**Acceso:** ciudadano propietario, `OPERADOR` o `RESPONSABLE_ATENCION`.

**Recibe:** `id` del reporte en la URL. Sin cuerpo JSON.

**Responde:** `200 OK`, con una lista de `EvidenciaResponse`, ordenada por fecha de registro ascendente:

```json
[
  {
    "id": 5,
    "tipo": "IMAGEN",
    "referenciaFicticia": "simulada://alumbrado/foto-1",
    "descripcion": "Referencia ficticia de la lámpara",
    "fechaRegistro": "2026-10-08T10:35:00"
  }
]
```

Si el reporte existe y no tiene evidencias, devuelve `[]`. No está paginado.

**Errores propios:** `403` por consultar un reporte de otro ciudadano; `404` si el reporte no existe.

### POST `/api/v1/reportes/{id}/evidencias`

**Propósito:** registrar una referencia ficticia de evidencia. No sube archivos ni utiliza `multipart/form-data`.

**Acceso:** únicamente el ciudadano propietario.

**Recibe:** `id` del reporte en la URL y `EvidenciaCreateRequest` en el cuerpo.

| Campo | Obligatorio | Validación |
| --- | --- | --- |
| `tipo` | Sí | No vacío; máximo 50 caracteres; texto libre, por ejemplo `IMAGEN` |
| `referenciaFicticia` | Sí | No vacía; máximo 255 caracteres |
| `descripcion` | No | Máximo 255 caracteres |

```json
{
  "tipo": "IMAGEN",
  "referenciaFicticia": "simulada://alumbrado/foto-1",
  "descripcion": "Referencia ficticia de la lámpara"
}
```

No se envían `reporteId`, `id` ni `fechaRegistro` en el cuerpo. El reporte procede de la URL y la fecha la establece el servidor.

**Responde:** `201 Created`, con un objeto `EvidenciaResponse` del formato mostrado en la consulta. Incluye la cabecera `Location: /api/v1/reportes/12/evidencias` para el reporte del ejemplo.

**Errores propios:** `400` por entrada inválida; `403` por rol no autorizado o reporte ajeno; `404` si el reporte no existe.

## 6. SeguimientoController: historial de estados

### GET `/api/v1/reportes/{id}/seguimientos`

**Propósito:** conocer cómo ha cambiado el estado de un reporte, con los comentarios y roles que realizaron cada cambio.

**Acceso:** ciudadano propietario, `OPERADOR` o `RESPONSABLE_ATENCION`.

**Recibe:** `id` del reporte en la URL. Sin cuerpo JSON.

**Responde:** `200 OK`, con una lista de `SeguimientoResponse`, ordenada por fecha ascendente y luego por identificador ascendente:

```json
[
  {
    "id": 7,
    "estadoAnterior": "REGISTRADO",
    "estadoNuevo": "EN_REVISION",
    "comentario": "Se revisará la información del reporte",
    "actorRol": "OPERADOR",
    "fecha": "2026-10-08T11:00:00"
  }
]
```

Si todavía no hubo cambios de estado, devuelve `[]`. La creación inicial del reporte no agrega un seguimiento. El historial no está paginado.

No existe un POST de seguimientos: se crean mediante `PATCH /api/v1/reportes/{id}/estado`, para mantener juntos el cambio y su historial. El cliente no establece `estadoAnterior`, `actorRol` ni `fecha`.

**Errores propios:** `403` por consultar un reporte de otro ciudadano; `404` si el reporte no existe.

## 7. EventoContextoController: eventos ambientales simulados

### POST `/api/v1/eventos-contexto`

**Propósito:** asociar a un reporte una medición ambiental simulada. No se conecta con dispositivos IoT ni toma mediciones reales.

**Acceso:** únicamente `RESPONSABLE_ATENCION`.

**Recibe:** `EventoContextoCreateRequest`. En esta ruta el identificador del reporte se envía en el cuerpo.

| Campo | Obligatorio | Validación |
| --- | --- | --- |
| `reporteId` | Sí | Número positivo; reporte existente |
| `fuente` | Sí | No vacía; máximo 255 caracteres |
| `variable` | Sí | No vacía; máximo 255 caracteres |
| `unidad` | Sí | No vacía; máximo 255 caracteres |
| `valor` | Sí | Número decimal; máximo 13 dígitos enteros y 6 decimales |
| `interpretacion` | No | Máximo 255 caracteres |

```json
{
  "reporteId": 12,
  "fuente": "SIMULADOR_AMBIENTAL",
  "variable": "temperatura",
  "unidad": "°C",
  "valor": 32.5,
  "interpretacion": "Temperatura simulada durante la atención"
}
```

`fuente`, `variable` y `unidad` son textos libres. `valor` puede ser negativo; no existe una validación específica por variable. El servidor establece `instante`, por lo que no debes enviarlo.

**Responde:** `201 Created`, con `EventoContextoResponse`:

```json
{
  "id": 3,
  "fuente": "SIMULADOR_AMBIENTAL",
  "instante": "2026-10-08T11:30:00",
  "variable": "temperatura",
  "unidad": "°C",
  "valor": 32.5,
  "interpretacion": "Temperatura simulada durante la atención"
}
```

Incluye `Location: /api/v1/reportes/12/eventos-contexto` para el ejemplo. No modifica automáticamente el estado ni la prioridad del reporte.

**Errores propios:** `400` por entrada inválida; `403` por rol no autorizado; `404` si el reporte no existe.

### GET `/api/v1/reportes/{id}/eventos-contexto`

**Propósito:** consultar los eventos simulados asociados a un reporte.

**Acceso:** ciudadano propietario, `OPERADOR` o `RESPONSABLE_ATENCION`.

**Recibe:** `id` del reporte en la URL. Sin cuerpo JSON.

**Responde:** `200 OK`, con una lista de `EventoContextoResponse`, cuyos objetos tienen el formato de la respuesta anterior. Se ordenan por `instante` ascendente. Si el reporte existe y no tiene eventos, devuelve `[]`. No está paginado.

**Errores propios:** `403` por consultar un reporte de otro ciudadano; `404` si el reporte no existe.

## Errores comunes

Todas las rutas protegidas pueden devolver `401` si falta el token, es inválido o expiró. Las operaciones que consultan la identidad también comprueban que el usuario esté habilitado. En las URLs, un identificador que no puede convertirse a número produce `400`.

| HTTP | Significado |
| --- | --- |
| `200 OK` | Consulta, login o cambio completado |
| `201 Created` | Recurso creado |
| `400 Bad Request` | JSON, parámetros o datos inválidos |
| `401 Unauthorized` | Autenticación requerida o credenciales inválidas |
| `403 Forbidden` | Rol sin permiso o reporte ajeno |
| `404 Not Found` | Recurso inexistente |
| `409 Conflict` | Correo duplicado, recurso inactivo, transición inválida o conflicto de integridad/concurrencia |
| `500 Internal Server Error` | Error inesperado, sin detalles internos en la respuesta |

Las respuestas de error usan `ApiError`:

```json
{
  "code": "REPORTE_NOT_FOUND",
  "message": "El recurso solicitado no existe",
  "timestamp": "2026-10-08T12:00:00",
  "path": "/api/v1/reportes/999"
}
```

`code` identifica el error; `message` lo explica; `timestamp` indica cuándo ocurrió; `path` indica la ruta solicitada. Otros códigos existentes son `INVALID_INPUT`, `INVALID_PAGINATION`, `INVALID_CREDENTIALS`, `EMAIL_EXISTS`, `INACTIVE_RESOURCE`, `FORBIDDEN`, `FORBIDDEN_TRANSITION`, `INVALID_TRANSITION` y `CONFLICT`.

Las fechas de los recursos y errores se representan como fecha y hora sin zona horaria (`LocalDateTime`).

## Rutas auxiliares de Spring

Estas rutas no pertenecen a los controllers de negocio, pero están disponibles en la aplicación:

| Método | Ruta | Acceso | Utilidad |
| --- | --- | --- | --- |
| GET | `/actuator/health` | Público | Verificar la salud del backend; normalmente responde `200` con `{"status":"UP"}`; puede devolver `503` si no está saludable |
| GET | `/v3/api-docs` | Público | Obtener la especificación OpenAPI en JSON |
| GET | `/swagger-ui/index.html` | Público | Abrir la documentación interactiva y probar solicitudes |

Para probar rutas protegidas en Swagger, utiliza **Authorize** e introduce el JWT.

## Flujo de uso sugerido

1. Registra un ciudadano mediante `/auth/register` o inicia sesión mediante `/auth/login`.
2. Guarda `accessToken` y envíalo en la cabecera `Authorization`.
3. Consulta `/categorias` y `/zonas` para obtener identificadores activos.
4. Crea un reporte mediante `POST /reportes`.
5. Registra evidencias ficticias con el identificador devuelto.
6. Consulta `/mis-reportes`, el detalle o el historial para conocer su avance.
7. Un operador o responsable previamente habilitado realiza las transiciones que le corresponden; el responsable puede registrar eventos simulados.
