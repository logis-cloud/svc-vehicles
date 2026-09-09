# Microservicio de Vehículos y Flota — Sistema de Logística y Entregas

API REST (Java + Spring Boot + PostgreSQL) para gestionar la flota de vehículos
y sus conductores. Es uno de los 3 microservicios "con base de datos propia"
del proyecto (los otros dos son Clientes [Python/FastAPI/MySQL] y Envíos
[Node.js/MongoDB]).

## Stack técnico

- **Java 17** + **Spring Boot 3.3**
- **Spring Data JPA** (Hibernate) sobre **PostgreSQL 16**
- **Bean Validation** (jakarta.validation) para validar los DTOs de entrada
- **springdoc-openapi** para Swagger UI
- **datafaker** para generar datos ficticios de prueba
- **Maven** + **Docker / Docker Compose**

## Diagrama Entidad/Relación (PostgreSQL)

```
┌───────────────────────────┐          ┌──────────────────────────────────────┐
│         vehiculos          │ 1      N │              conductores               │
├───────────────────────────┤──────────├──────────────────────────────────────┤
│ id_vehiculo (PK)          │          │ id_conductor (PK)                     │
│ placa (UNIQUE)            │          │ id_vehiculo (FK -> vehiculos, NULL)   │
│ tipo (moto/furgoneta/     │          │ nombre                                │
│       camion)             │          │ apellido                              │
│ capacidad_kg              │          │ nro_licencia (UNIQUE)                 │
│ marca                     │          │ dni (UNIQUE)                          │
│ modelo                    │          │ telefono                              │
│ anio_fabricacion          │          │ turno (mañana/tarde/noche)            │
│ estado (disponible/       │          │ fecha_contratacion                    │
│  en_ruta/mantenimiento/   │          │ activo                                │
│  inactivo)                │          └──────────────────────────────────────┘
│ fecha_registro            │
└───────────────────────────┘
```

Relación: **1 vehículo → N conductores** (`ON DELETE SET NULL`).

### Por qué 1 → N y no 1 → 1

En una flota real los conductores suelen **rotar** entre vehículos según el
`turno` (mañana / tarde / noche): un mismo camión puede tener un conductor en
la mañana y otro distinto en la tarde. Por eso la FK `id_vehiculo` vive en
`conductores` (no al revés) y es **nullable**: un conductor puede existir sin
tener un vehículo asignado en este momento (recién contratado, de reserva,
entre asignaciones), y un vehículo puede tener históricamente varios
conductores.

Si tu operación decide que cada conductor tiene **un único vehículo fijo**
(1 → 1), el cambio es mínimo: agregar una restricción `UNIQUE` sobre
`conductores.id_vehiculo`. El resto del modelo, endpoints y código no cambian.

Al eliminar un vehículo, sus conductores **no se eliminan**: `id_vehiculo`
queda en `NULL` (a diferencia del microservicio de Clientes, donde eliminar
un cliente sí borra en cascada sus direcciones).

### Campos agregados sobre el enunciado original

| Tabla | Campo agregado | Motivo |
|---|---|---|
| vehiculos | `marca`, `modelo`, `anio_fabricacion` | Trazabilidad y mantenimiento de flota |
| vehiculos | `estado` | Permite marcar `en_ruta` / `mantenimiento` — lo puede actualizar el MS de Envíos vía `PATCH /vehiculos/{id}/estado` |
| vehiculos | `fecha_registro` | Auditoría, útil para la ingesta del módulo de Data Science |
| conductores | `apellido`, `dni`, `telefono` | Datos básicos de identificación del personal |
| conductores | `fecha_contratacion`, `activo` | Gestión de personal (bajas sin borrar historial) |

## Endpoints principales

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/vehiculos` | Lista paginada de vehículos (filtros: `tipo`, `estado`) |
| GET | `/vehiculos/{id}` | Detalle de un vehículo + sus conductores asignados |
| POST | `/vehiculos` | Crea un vehículo |
| PUT | `/vehiculos/{id}` | Actualiza los datos de un vehículo |
| PATCH | `/vehiculos/{id}/estado` | Cambia el estado operativo (usado por el MS de Envíos) |
| DELETE | `/vehiculos/{id}` | Elimina un vehículo (sus conductores quedan sin asignar) |
| GET | `/vehiculos/{id}/conductores` | Conductores asignados a un vehículo |
| GET | `/conductores` | Lista paginada de conductores (filtros: `turno`, `idVehiculo`, `activo`) |
| GET | `/conductores/{id}` | Detalle de un conductor |
| POST | `/conductores` | Crea un conductor (opcionalmente con vehículo asignado) |
| PUT | `/conductores/{id}` | Actualiza los datos de un conductor |
| PATCH | `/conductores/{id}/asignar-vehiculo/{idVehiculo}` | Asigna/reasigna vehículo (rotación por turno) |
| PATCH | `/conductores/{id}/liberar` | Libera al conductor de su vehículo actual |
| DELETE | `/conductores/{id}` | Elimina un conductor |
| POST | `/admin/seed` | *(solo dev, requiere `SEED_ENABLED=true`)* Carga datos ficticios |

Swagger UI: **`/docs`** — Especificación OpenAPI (JSON): **`/v3/api-docs`**

Health check (Actuator): **`/actuator/health`**

## Cómo correrlo en local

```bash
cp .env.example .env
docker compose up --build
```

- API disponible en `http://localhost:8002`
- Swagger en `http://localhost:8002/docs`
- PostgreSQL en `localhost:5433` (solo expuesto en desarrollo; internamente el contenedor usa el puerto 5432)

## Cargar datos ficticios

El seeder está apagado por defecto. Para habilitarlo, en `.env`:

```
SEED_ENABLED=true
```

Con el stack corriendo:

```bash
curl -X POST "http://localhost:8002/admin/seed?vehiculos=3000&conductores=4000"
```

Esto inserta (por defecto) **3,000 vehículos** y **4,000 conductores** usando
`datafaker`, guardando en lotes de 500 para que sea rápido. Los volúmenes son
configurables por query param (`?vehiculos=...&conductores=...`).

> Nota: una flota real es naturalmente mucho más pequeña que la base de
> clientes, por eso los valores por defecto aquí son menores que los 25,000
> del microservicio de Clientes. Si el módulo de ingesta de Data Science
> necesita más volumen para las pruebas de la estrategia *pull* hacia S3,
> simplemente se aumentan los parámetros de la llamada.

## Manejo de errores

Todas las respuestas de error siguen el mismo formato JSON:

```json
{
  "timestamp": "2026-09-03T10:15:00",
  "status": 404,
  "error": "Not Found",
  "message": "Vehículo no encontrado con id 99",
  "path": "/vehiculos/99"
}
```

| Código | Cuándo ocurre |
|---|---|
| 400 | Error de validación de los datos de entrada (incluye `detalles` con campo -> mensaje) |
| 404 | El vehículo/conductor solicitado no existe |
| 409 | Conflicto de negocio: placa/DNI/nro. de licencia duplicados, o intento de asignar un vehículo `INACTIVO` |
| 500 | Error interno no controlado |

## Mapeo a la arquitectura de producción pedida en el enunciado

- Este contenedor (`ms-vehiculos`) se despliega junto a los otros 4
  microservicios en **2 Máquinas Virtuales** detrás de un **balanceador de
  carga privado**.
- Se expone al público solo a través de **AWS API Gateway (HTTPS)**.
- La base de datos PostgreSQL de este microservicio corre en la **tercera
  MV** (privada, no pública), junto con MySQL (Clientes) y MongoDB (Envíos).

## Próximos pasos sugeridos

- Tests unitarios/de integración (servicios + repositorios con Testcontainers)
- Autenticación entre microservicios (API Gateway + JWT/mTLS interno)
- Endpoint de reporte de disponibilidad de flota por tipo/estado para el
  microservicio agregador
