# Reportes Ciudadanos

Backend REST en Java 21, Spring Boot 3.5.7, Maven y PostgreSQL. No incluye frontend. El proyecto se organiza por capas: cada carpeta agrupa todas las clases de una misma responsabilidad. Los DTOs de entrada están en `dto/request` y los de salida en `dto/response`. La autenticación usa BCrypt y JWT HS256 mediante Spring Security OAuth2 Resource Server.


## Organización

```text
src/main/java/com/reportesciudadanos/
├── ReportesCiudadanosApplication.java
├── config/          # Configuración de seguridad y OpenAPI
├── controller/      # Todos los controladores REST
├── dto/
│   ├── request/     # Solicitudes de entrada
│   └── response/    # Respuestas, errores y paginación
├── entity/          # Todas las entidades JPA
├── enums/           # Estados, prioridades y roles
├── exception/       # Excepciones y manejo global
├── mapper/          # Transformación entre entidades y DTOs
├── repository/      # Todos los repositorios JPA
├── service/         # Servicios de negocio, identidad y JWT
└── validation/      # Reglas de transición
```

## Ejecutar

Requisitos: JDK 21, Maven, Docker y el contenedor existente `inventario-postgres`. Se verificó que publica `5432` y usa `postgres`; se creó únicamente la base `reportes_ciudadanos`.

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:$PATH"
export JWT_SECRET="$(openssl rand -base64 48)"
./scripts/run-local.sh
```

El script obtiene las credenciales del contenedor sin imprimirlas y respeta su puerto publicado. Requiere acceso al daemon Docker. La base debe existir; el script no modifica otras bases. Flyway aplica las migraciones y Hibernate valida el esquema.

Alternativamente, configura `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` y opcionalmente `JWT_TTL_SECONDS` (3600) y `CORS_ORIGINS` (orígenes separados por comas). Consulta `.env.example`: Spring no carga archivos `.env` automáticamente. Guarda el secreto JWT de forma persistente para que los tokens sobrevivan a reinicios. No uses el secreto de pruebas en producción.

```bash
mvn test
mvn package
java -jar target/reportes-ciudadanos-1.0.0.jar
```

Swagger: http://localhost:8080/swagger-ui/index.html. OpenAPI: `/v3/api-docs`. Salud pública sin detalles: `/actuator/health`.

## Uso

Consulta [RUTAS_API.md](RUTAS_API.md) para conocer cada endpoint, sus permisos, campos de entrada y ejemplos de respuesta.

```bash
curl -X POST localhost:8080/api/v1/auth/register -H 'Content-Type: application/json' \
  -d '{"nombre":"Ciudadano de prueba","correoElectronico":"demo@example.test","password":"EjemploSeguro123!"}'
```

Usa `accessToken` como `Authorization: Bearer TOKEN`. El registro público asigna exclusivamente CIUDADANO. No se aceptan campos desconocidos, propietario, fechas ni estado inicial en solicitudes de creación.

```bash
curl -X POST localhost:8080/api/v1/reportes -H 'Authorization: Bearer TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{"categoriaId":1,"zonaId":1,"titulo":"Alumbrado dañado","descripcion":"Lámpara apagada en la calle","prioridad":"MEDIA"}'
```

Endpoints bajo `/api/v1`:

| Método | Ruta | Uso |
| --- | --- | --- |
| POST | /auth/register, /auth/login | Registro ciudadano e inicio de sesión |
| POST | /reportes | Crear reporte propio |
| GET | /reportes | Listado paginado, restringido al propietario para ciudadanos |
| GET | /reportes/mis-reportes | Listado del usuario autenticado |
| GET | /reportes/{id} | Detalle con evidencias, seguimientos y eventos |
| PATCH | /reportes/{id}/estado | Cambiar estado y registrar seguimiento atómicamente |
| GET | /categorias, /categorias/{id} | Catálogo activo |
| GET | /zonas, /zonas/{id} | Catálogo activo |
| GET, POST | /reportes/{id}/evidencias | Evidencias ficticias; alta por ciudadano propietario |
| GET | /reportes/{id}/seguimientos | Historial |
| POST | /eventos-contexto | Registrar evento simulado por responsable |
| GET | /reportes/{id}/eventos-contexto | Consultar eventos |

Listado: `page=0&size=20&sort=fechaCreacion&estado=REGISTRADO&prioridad=MEDIA&categoriaId=1&zonaId=1`. Tamaño máximo 100; orden descendente por `id`, `fechaCreacion`, `prioridad` o `estado`. No se cargan colecciones ni relaciones en listados. El detalle consulta cada colección por separado para evitar productos cartesianos y N+1.

## Permisos propuestos de transición

La especificación no fija la matriz definitiva: esta implementación adopta la siguiente propuesta ajustable en `TransitionPolicy`.

| Rol | Transiciones |
| --- | --- |
| OPERADOR | REGISTRADO → EN_REVISION; EN_REVISION → REGISTRADO o EN_ATENCION |
| RESPONSABLE_ATENCION | EN_ATENCION → EN_REVISION o RESUELTO; RESUELTO → EN_ATENCION o CERRADO |
| CIUDADANO | Ninguna |

Todas las demás transiciones se rechazan. Los dos roles de personal consultan todos los reportes. Ciudadanos consultan únicamente los propios y pueden adjuntar evidencias ficticias. Los eventos se generan mediante solicitudes explícitas, sin integración IoT. El historial se registra a través del cambio de estado: no existe un endpoint redundante para cambiar estados indirectamente.

No hay eliminación, administración pública de usuarios ni asignación pública de roles privilegiados. Para probar personal, un administrador de base puede crear un usuario con hash BCrypt generado mediante una herramienta de confianza y relacionarlo con el rol existente; no se suministran contraseñas predeterminadas. El propietario siempre se obtiene de la identidad autenticada. Los cambios concurrentes usan bloqueo optimista y devuelven 409 si existe conflicto.

Los JWT expiran y se verifican firma e issuer. El servicio también verifica que el usuario permanezca habilitado y usa su rol actual al autorizar transiciones. No hay refresh tokens ni revocación individual en esta versión; al expirar se requiere iniciar sesión nuevamente.

## Pruebas

`mvn test` ejecuta pruebas Spring Boot/MockMvc con H2 en modo PostgreSQL y las mismas migraciones Flyway, además de una matriz exhaustiva de transiciones. Las pruebas cubren creación, referencias inexistentes, detalle, propiedad, permisos, historial atómico, autenticación JWT real, firma inválida y paginación. H2 permite ejecutar sin tocar las bases PostgreSQL existentes; la verificación local con PostgreSQL se documenta en `VERIFICATION.md`.

Compatibilidad consultada: [Spring Boot 3.5](https://docs.spring.io/spring-boot/3.5/system-requirements.html) y [matriz springdoc](https://springdoc.org/v2/).
