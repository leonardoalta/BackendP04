# Verificación realizada

Fecha local: 8 de octubre de 2026. JDK OpenJDK 21.0.12.

- `mvn package`: exitoso. En el entorno restringido se utilizó `-Dmaven.repo.local=/tmp/reportes-m2` para no escribir en la caché personal.
- 14 pruebas JUnit: 12 de integración Spring Boot/MockMvc, una de Mockito y una matriz de 75 combinaciones de estado origen, destino y rol. Cero fallos, errores o pruebas omitidas.
- PostgreSQL existente: contenedor `inventario-postgres`, activo, puerto host 5432, usuario verificado `postgres`. Se creó `reportes_ciudadanos`; no se modificaron las otras bases.
- Arranque real del JAR contra PostgreSQL: correcto, con Flyway V1 y V2 aplicadas y esquema validado por Hibernate.
- Verificación HTTP real en puerto 8081: salud 200, acceso sin token 401, registro 201, login 200, categorías 200, creación de reporte 201, detalle 200, evidencia ficticia 201, cambio de estado por ciudadano 403 y OpenAPI 200.
- OpenAPI verificó que POST de reportes documenta 201.

Se conservaron en la nueva base un usuario de prueba con correo `@example.test`, un reporte y una evidencia ficticia. No se agregaron usuarios privilegiados.

Para repetir la verificación con el backend iniciado:

```bash
API_URL=http://localhost:8080 python scripts/smoke.py
```

Cada ejecución crea datos ficticios nuevos. Las pruebas automatizadas usan H2 y no afectan PostgreSQL. La ejecución temporal de verificación se detuvo al terminar.

## Reorganización por capas

Se trasladaron las 64 clases auxiliares a carpetas por responsabilidad y se actualizaron los paquetes, imports y referencias de las pruebas. Los DTOs quedaron separados en `dto/request` y `dto/response`. Se ejecutó nuevamente `mvn clean package` con Java 21: las 14 pruebas pasaron y se regeneró el JAR ejecutable. La prueba de integración arrancó el contexto Spring y validó las migraciones y los endpoints con la nueva estructura.
