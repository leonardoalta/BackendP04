# Sistema de Reportes Ciudadanos

Este backend permite registrar incidencias de una comunidad, consultar su avance y gestionar su atención. Expone una API REST que se puede consumir desde Angular, Postman, Swagger o la terminal.

Utiliza Java 21, Spring Boot 3.5.7, Maven y PostgreSQL. El login devuelve un token JWT; las contraseñas se guardan con BCrypt. Las evidencias son referencias ficticias y los eventos ambientales son simulados.

## 1. Preparar la terminal

Los siguientes pasos están pensados para Linux y para el contenedor existente `inventario-postgres`.

Abre una terminal en la carpeta del proyecto:

```bash
cd /home/leo/Universidad/Semestre_7/Desarrollo_Web/Actividad4
```

Selecciona Java 21 en esta terminal:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:$PATH"
```

Comprueba las herramientas necesarias:

```bash
java -version
mvn -version
docker --version
openssl version
```

`java -version` y `mvn -version` deben indicar Java **21**. Si utilizas otra instalación de Java 21, cambia `JAVA_HOME` por su ruta.

## 2. Comprobar PostgreSQL

El proyecto utiliza el contenedor `inventario-postgres`. No necesitas crear otro.

```bash
docker ps -a --filter name=inventario-postgres
docker start inventario-postgres
```

Comprueba el puerto publicado:

```bash
docker port inventario-postgres 5432
```

En la configuración verificada para este proyecto, el puerto del host es `5432`, el usuario es `postgres` y la base `reportes_ciudadanos` ya está creada.

Para consultar las bases existentes sin modificarlas:

```bash
docker exec inventario-postgres sh -c \
  'psql -U "${POSTGRES_USER:-postgres}" -d postgres -c "\l"'
```

**Solo si `reportes_ciudadanos` no aparece**, créala:

```bash
docker exec inventario-postgres sh -c \
  'psql -U "${POSTGRES_USER:-postgres}" -d postgres -v ON_ERROR_STOP=1 -c "CREATE DATABASE reportes_ciudadanos"'
```

El backend crea sus tablas mediante Flyway al iniciar y luego valida el esquema. No necesitas crear las tablas manualmente ni modificar otras bases.

## 3. Iniciar el backend

En la misma terminal, genera una clave para firmar los tokens:

```bash
export JWT_SECRET="$(openssl rand -base64 48)"
```

Inicia la aplicación:

```bash
./scripts/run-local.sh
```

El script arranca el contenedor si está detenido, obtiene su usuario, contraseña y puerto, y ejecuta el backend con Maven. No imprime las credenciales. La clave JWT debe contener al menos 32 bytes.

Espera hasta ver un mensaje similar a:

```text
Started ReportesCiudadanosApplication
```

La aplicación escucha normalmente en **http://localhost:8080**. Deja esta terminal abierta mientras pruebas; para detenerla presiona **Ctrl+C**.

Cada vez que generas una clave JWT diferente, los tokens anteriores dejan de ser válidos. Para reutilizar tokens entre reinicios, conserva la misma clave de forma segura y vuelve a exportarla.

## 4. Confirmar que está funcionando

Abre una **segunda terminal** y ejecuta:

```bash
curl -i http://localhost:8080/actuator/health
```

Debes recibir `200 OK` y un cuerpo como:

```json
{"status":"UP"}
```

También puedes abrir en el navegador:

- [Swagger UI](http://localhost:8080/swagger-ui/index.html): muestra los endpoints y permite probarlos.
- [OpenAPI](http://localhost:8080/v3/api-docs): especificación de la API en JSON.

## 5. Registrar un ciudadano

Ejecuta en la segunda terminal:

```bash
curl -i -X POST http://localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{
    "nombre": "Ciudadano de prueba",
    "correoElectronico": "demo@example.test",
    "password": "EjemploSeguro123!"
  }'
```

Debes recibir `201 Created` y una respuesta de este formato:

```json
{
  "accessToken": "TOKEN_GENERADO_POR_EL_SERVIDOR",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

El registro crea únicamente usuarios con rol **CIUDADANO**. La contraseña debe tener al menos 12 caracteres y no superar 72 bytes UTF-8. Si el correo ya existe, recibirás `409`; utiliza el login del siguiente paso.

## 6. Hacer login y guardar el token

```bash
curl -i -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{
    "correoElectronico": "demo@example.test",
    "password": "EjemploSeguro123!"
  }'
```

Si las credenciales son correctas, recibirás `200 OK` y un `accessToken`. Copia **solo el valor** de ese campo y guárdalo en una variable de la segunda terminal:

```bash
export TOKEN='PEGA_AQUI_EL_ACCESS_TOKEN_COMPLETO'
```

Sustituye el texto de ejemplo por el token real. El token dura por defecto una hora. Cuando expire, vuelve a hacer login y actualiza `TOKEN`.

En Postman, selecciona **Authorization → Bearer Token** y pega el token. En Swagger, presiona **Authorize** e introduce el token.

## 7. Consultar categorías y zonas

Antes de crear un reporte, consulta qué identificadores están disponibles:

```bash
curl -i http://localhost:8080/api/v1/categorias \
  -H "Authorization: Bearer $TOKEN"
```

```bash
curl -i http://localhost:8080/api/v1/zonas \
  -H "Authorization: Bearer $TOKEN"
```

Ambas rutas devuelven `200 OK` y listas de objetos con `id` y `nombre`. La migración inicial agrega categorías como Alumbrado y Limpieza, y zonas como Centro y Norte.

## 8. Crear y consultar un reporte

Usa identificadores activos obtenidos en el paso anterior. El ejemplo utiliza categoría `1` y zona `1`:

```bash
curl -i -X POST http://localhost:8080/api/v1/reportes \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "categoriaId": 1,
    "zonaId": 1,
    "titulo": "Alumbrado dañado",
    "descripcion": "Una lámpara de la calle permanece apagada",
    "prioridad": "MEDIA"
  }'
```

Debes recibir `201 Created`. La respuesta contiene el `id` del reporte y su estado inicial `REGISTRADO`. La prioridad puede ser `BAJA`, `MEDIA` o `ALTA`. El servidor asigna el propietario y las fechas; no los envíes en el cuerpo.

Guarda el identificador devuelto. Por ejemplo, **si la respuesta contiene `"id": 12`**:

```bash
export REPORTE_ID=12
```

Consulta su detalle completo:

```bash
curl -i "http://localhost:8080/api/v1/reportes/$REPORTE_ID" \
  -H "Authorization: Bearer $TOKEN"
```

La respuesta contiene `reporte`, `evidencias`, `seguimientos` y `eventosContexto`. Las listas estarán vacías hasta que existan registros asociados.

Consulta tus reportes con paginación:

```bash
curl -i 'http://localhost:8080/api/v1/reportes/mis-reportes?page=0&size=20' \
  -H "Authorization: Bearer $TOKEN"
```

`page=0` es la primera página. `size` permite entre 1 y 100 elementos. La respuesta incluye `content`, `page`, `size`, `totalElements` y `totalPages`.

## 9. Agregar una evidencia y consultar el historial

Como ciudadano propietario, puedes registrar una referencia ficticia; esta ruta no sube archivos:

```bash
curl -i -X POST "http://localhost:8080/api/v1/reportes/$REPORTE_ID/evidencias" \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "tipo": "IMAGEN",
    "referenciaFicticia": "simulada://alumbrado/foto-1",
    "descripcion": "Referencia ficticia de la lámpara"
  }'
```

Debe devolver `201 Created`. Para consultar el historial de estados:

```bash
curl -i "http://localhost:8080/api/v1/reportes/$REPORTE_ID/seguimientos" \
  -H "Authorization: Bearer $TOKEN"
```

Devuelve `200 OK`. La lista es `[]` hasta que un operador o responsable cambie el estado.

## 10. Entender los roles y los cambios de estado

| Rol | Permisos actuales |
| --- | --- |
| CIUDADANO | Crear reportes propios, consultar sus datos e historial, agregar evidencias y consultar catálogos |
| OPERADOR | Consultar todos los reportes y realizar los cambios de revisión que le corresponden |
| RESPONSABLE_ATENCION | Consultar todos los reportes, gestionar atención y cierre, y registrar eventos ambientales simulados |

Las transiciones implementadas son:

| Rol | Estado actual | Estado de destino |
| --- | --- | --- |
| OPERADOR | REGISTRADO | EN_REVISION |
| OPERADOR | EN_REVISION | REGISTRADO o EN_ATENCION |
| RESPONSABLE_ATENCION | EN_ATENCION | EN_REVISION o RESUELTO |
| RESPONSABLE_ATENCION | RESUELTO | EN_ATENCION o CERRADO |

Un ciudadano no puede cambiar estados. Cada transición válida registra automáticamente un seguimiento con comentario, fecha y rol del actor. El cambio y su historial se guardan en una misma transacción.

Para probar una transición necesitas una cuenta de personal previamente habilitada por el administrador de la base. No hay cuentas privilegiadas predeterminadas ni un endpoint público para asignar estos roles. Haz login con esa cuenta y guarda su token:

```bash
export TOKEN_OPERADOR='PEGA_AQUI_EL_TOKEN_DEL_OPERADOR'
```

Para un reporte en `REGISTRADO`, un operador puede ejecutar:

```bash
curl -i -X PATCH "http://localhost:8080/api/v1/reportes/$REPORTE_ID/estado" \
  -H "Authorization: Bearer $TOKEN_OPERADOR" \
  -H 'Content-Type: application/json' \
  -d '{
    "estado": "EN_REVISION",
    "comentario": "Se revisará la información del reporte"
  }'
```

Debe devolver `200 OK` con el reporte actualizado. Con el token de un ciudadano devuelve `403`. Una transición que no está permitida devuelve `409`. Desde `CERRADO` no se permiten más cambios. La matriz puede ajustarse en `validation/TransitionPolicy.java`.

## 11. Ejecutar las pruebas y generar el JAR

Desde la carpeta del proyecto, con Java 21 seleccionado:

```bash
mvn test
```

Hay **14 pruebas automatizadas** que cubren creación, referencias inexistentes, consulta, propiedad, permisos, cambios de estado, historial, JWT y paginación. Utilizan una base H2 de pruebas; no modifican tu PostgreSQL. El resultado esperado es cero fallos y `BUILD SUCCESS`.

Para compilar, ejecutar las pruebas y generar el archivo ejecutable:

```bash
mvn clean package
```

Se genera `target/reportes-ciudadanos-1.0.0.jar`.

Para repetir la comprobación HTTP con el backend ya iniciado, ejecuta en otra terminal:

```bash
python scripts/smoke.py
```

Este script registra un usuario nuevo, crea un reporte ficticio y una evidencia, y comprueba autenticación, permisos y documentación. Conserva esos datos en `reportes_ciudadanos`. Requiere Python 3.

Los resultados de las verificaciones realizadas están en [VERIFICATION.md](VERIFICATION.md).

## 12. Configuración manual e inicio desde el IDE

La configuración está en `src/main/resources/application.properties`. Sus valores de conexión y seguridad proceden de variables de entorno:

| Variable | Para qué sirve | Valor o ejemplo |
| --- | --- | --- |
| `DB_URL` | Dirección de PostgreSQL | `jdbc:postgresql://localhost:5432/reportes_ciudadanos` |
| `DB_USERNAME` | Usuario de PostgreSQL | Usuario verificado del contenedor; aquí `postgres` |
| `DB_PASSWORD` | Contraseña de PostgreSQL | Contraseña real del contenedor |
| `JWT_SECRET` | Clave para firmar los tokens | Obligatoria; mínimo 32 bytes |
| `JWT_TTL_SECONDS` | Duración del token | Opcional; por defecto `3600` |
| `CORS_ORIGINS` | Orígenes permitidos para el navegador | Opcional; por defecto `http://localhost:4200`; varios separados por comas |

Para iniciar sin el script, exporta estas variables. Los siguientes comandos obtienen las credenciales del contenedor sin escribirlas directamente:

```bash
export DB_USERNAME="$(docker exec inventario-postgres sh -c 'printf %s "${POSTGRES_USER:-postgres}"')"
export DB_PASSWORD="$(docker exec inventario-postgres sh -c 'printf %s "$POSTGRES_PASSWORD"')"
export DB_URL='jdbc:postgresql://localhost:5432/reportes_ciudadanos'
export JWT_SECRET="$(openssl rand -base64 48)"
mvn spring-boot:run
```

Comprueba el puerto con `docker port` y ajusta `DB_URL` si no es `5432`. El contenedor debe estar iniciado. Estas variables pertenecen a esa terminal.

En el IDE, selecciona JDK 21 y configura `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` y `JWT_SECRET` en las variables de entorno de la configuración de ejecución de `ReportesCiudadanosApplication`. Reinicia la aplicación después de cambiar la configuración. El IDE no necesariamente recibe las variables exportadas en otra terminal.

También puedes ejecutar el JAR, con las mismas variables configuradas y el backend anterior detenido:

```bash
java -jar target/reportes-ciudadanos-1.0.0.jar
```

`.env.example` es una plantilla: Spring Boot no carga archivos `.env` automáticamente. No guardes contraseñas o claves reales en el repositorio.

## 13. Solucionar errores frecuentes

| Problema | Qué revisar |
| --- | --- |
| Maven utiliza Java 17 | Ejecuta los comandos de `JAVA_HOME` y `PATH` del paso 1; comprueba `mvn -version` |
| `permission denied` al usar Docker | Tu usuario necesita acceso al daemon Docker; utiliza una terminal con acceso antes de ejecutar el script |
| No existe `inventario-postgres` | Comprueba que estás usando el Docker donde está creado el contenedor existente |
| No existe `reportes_ciudadanos` | Sigue la comprobación y creación del paso 2 |
| Error de conexión o contraseña | Verifica contenedor activo, puerto publicado y credenciales reales |
| Falta `DB_URL`, `DB_PASSWORD` o `JWT_SECRET` | Usa el script o configura las variables en la terminal o el IDE que inicia la aplicación |
| Puerto 8080 ocupado | Detén la otra aplicación o ejecuta `./scripts/run-local.sh spring-boot:run -Dspring-boot.run.arguments=--server.port=8081`; usa después el puerto 8081 en las URLs |
| HTTP 400 | Revisa nombres de campos, validaciones y JSON; no envíes campos adicionales |
| HTTP 401 | Haz login y envía el token completo; comprueba que no expiró ni cambió la clave JWT |
| HTTP 403 | Revisa el rol y la propiedad del reporte; un ciudadano no consulta reportes ajenos ni cambia estados |
| HTTP 404 | Revisa que el identificador solicitado exista |
| HTTP 409 en registro | El correo ya existe; haz login o utiliza otro correo |
| HTTP 409 en cambio de estado | Comprueba el estado actual y las transiciones permitidas |

## 14. Organización del código

Las carpetas agrupan clases por responsabilidad:

```text
src/main/java/com/reportesciudadanos/
├── ReportesCiudadanosApplication.java
├── config/          # Configuración de seguridad y Swagger
├── controller/      # Recibe solicitudes HTTP y devuelve respuestas
├── dto/
│   ├── request/     # Campos que el cliente puede enviar
│   └── response/    # Campos que el servidor devuelve
├── entity/          # Representación de las tablas
├── enums/           # Estados, prioridades y roles
├── exception/       # Manejo de errores
├── mapper/          # Conversión entre entidades y DTOs
├── repository/      # Acceso a la base de datos
├── service/         # Reglas de negocio, identidad y JWT
└── validation/      # Validación de transiciones
```

Una solicitud sigue el flujo `Controller → Service → Repository → PostgreSQL`. Los controllers devuelven DTOs; los servicios validan las reglas y permisos.

Consulta [RUTAS_API.md](RUTAS_API.md) para ver **cada ruta**, sus campos de entrada, ejemplos de respuesta y errores. La documentación anterior describe el comportamiento implementado actualmente; no existen endpoints de eliminación o administración de usuarios.
