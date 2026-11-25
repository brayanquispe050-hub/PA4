# pedidos-reportes-service

Servicio REST Spring Boot que consulta los pedidos y sus revisiones mediante Spring JDBC. Requiere Java 21 y Maven (se incluye `mvnw.cmd`).

## Configuración y orden de inicio

El servicio usa el mismo esquema MySQL que `sistema-pedidos-springboot`. Inicie primero `inventario-service`, después `sistema-pedidos-springboot` para que cree/actualice las tablas PA4 y, finalmente, este servicio en el puerto `8083`. El usuario configurado aquí necesita permisos de lectura en `pedido` y `pedido_revision`.

Defina estas variables de entorno antes del inicio; no se incluyen credenciales predeterminadas en el repositorio:

| Variable | Uso |
|---|---|
| `DB_URL` | URL JDBC del esquema compartido; si se omite usa `jdbc:mysql://localhost:3306/pedidosdb?serverTimezone=America/Lima`. |
| `DB_USERNAME` | Usuario MySQL con permiso de lectura. |
| `DB_PASSWORD` | Contraseña MySQL. |

PowerShell:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/pedidosdb?serverTimezone=America/Lima"
$env:DB_USERNAME = "<usuario-local>"
$env:DB_PASSWORD = "<clave-local>"
.\mvnw.cmd spring-boot:run
```

La URL base es `http://localhost:8083/pedidos-reportes-service`.

## Esquema de revisiones

La aplicación principal define `pedido.revisado`, `pedido.estado_revision` (`PENDIENTE`, `ACEPTADO` o `RECHAZADO`) y `pedido_revision` con FK a `pedido.id` y clave única sobre `pedido_id`. La tabla de auditoría conserva observación, usuario y fecha. Hibernate actualiza el esquema en el arranque normal; para una base nueva aplicar `../../Sesión 5/sistema-pedidos-springboot/src/main/resources/db/pa4-schema.sql`. En una base PA4 existente, aplicar una vez `../../Sesión 5/sistema-pedidos-springboot/src/main/resources/db/pa4-resultado-migration.sql`; los pedidos ya revisados se consideran aceptados.

## API

| Método | Ruta | Comportamiento |
|---|---|---|
| GET | `/api/v1/pedidos/revisiones?estado=PENDIENTE` | Array JSON de pedidos pendientes. |
| GET | `/api/v1/pedidos/revisiones?estado=REVISADO` | Array JSON de pedidos supervisados, con resultado, observación y fecha. |
| GET | `/api/v1/pedidos/estadisticas/revisiones` | Totales y porcentaje de revisiones. |

El parámetro `estado` es obligatorio y distingue mayúsculas; acepta únicamente `PENDIENTE` o `REVISADO` (`200 OK`). Un parámetro ausente o inválido devuelve `400 Bad Request`.

Respuesta de `/api/v1/pedidos/revisiones?estado=REVISADO`:

```json
[
  {
    "pedidoId": 42,
    "cliente": "Ana Torres",
    "productoCodigo": "LAP-001",
    "productoNombre": "Laptop",
    "total": 2500.00,
    "estado": "REVISADO",
    "resultado": "RECHAZADO",
    "observacion": "Pedido verificado.",
    "revisadoPor": "operador",
    "fechaRevision": "2026-11-25T10:30:00"
  }
]
```

Respuesta de `/api/v1/pedidos/estadisticas/revisiones`:

```json
{"totalPedidos":12,"totalRevisados":4,"totalPendientes":8,"porcentajeRevisado":33.33}
```

Las consultas usan parámetros JDBC; los DTO HTTP no exponen entidades de persistencia.

## Pruebas

La suite usa H2 en modo MySQL y un `schema.sql` de prueba, sin requerir MySQL:

```powershell
.\mvnw.cmd test
```

Las pruebas de integración cubren filtros JSON, resultado de revisión, conteos, valores inválidos y respuesta HTTP. La aplicación no incorpora autenticación propia; limítese a una red de confianza. Este servicio es de solo lectura y depende de que la aplicación principal mantenga actualizado el esquema compartido.
