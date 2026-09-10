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

## Cargar datos ficticios (≥20,000 registros — requisito de la rúbrica)

El seeder está apagado por defecto. Para habilitarlo, en `.env`:

```
SEED_ENABLED=true
```

Con el stack corriendo:

```bash
curl -X POST "http://localhost:8002/admin/seed?vehiculos=4000&conductores=20000"
```

Esto inserta por defecto **4,000 vehículos** y **20,000 conductores** usando
`datafaker`, en lotes de 1,000 con commit por lote (igual que el patrón que
usa `seed_data.py` en el microservicio de Clientes), para que la carga sea
rápida y no dependa de una única transacción gigante. `conductores` es la
tabla de esta base de datos que cumple el mínimo de 20,000 registros pedido
por la rúbrica; se eligió esa y no `vehiculos` porque tiene más sentido de
negocio: una flota real es pequeña, pero el historial de personal/rotación
de conductores sí puede ser grande. Los volúmenes son configurables por
query param si necesitas más para las pruebas de ingesta hacia S3.

**Nota técnica:** DNI, número de licencia y placa se generan garantizando
unicidad dentro de la corrida (usando `Set` en memoria), no solo al azar.
A este volumen, generarlos completamente al azar sin control tiene una
probabilidad real de colisión contra las columnas `UNIQUE`, lo que rompería
el lote completo.

Como con cualquier seed masivo: **corre solo una vez** sobre una base de
datos vacía. Volver a correrlo no falla (los valores se siguen generando
únicos dentro de esa corrida), pero duplicará conceptualmente los datos.

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

> Nota de coherencia entre microservicios: el microservicio de Clientes (FastAPI)
> devuelve errores en el formato por defecto de FastAPI (`{"detail": "..."}`),
> distinto a este. Es una diferencia esperable entre stacks distintos, pero si
> el microservicio 4 (agregador) va a mostrar errores homogéneos al frontend,
> debe normalizar ambos formatos en su propia capa — no asumir un único shape.

## Mapeo a la arquitectura de producción pedida en el enunciado

- Este contenedor (`ms-vehiculos`) se despliega junto a los otros 4
  microservicios en **2 Máquinas Virtuales** detrás de un **balanceador de
  carga privado**.
- Se expone al público solo a través de **AWS API Gateway (HTTPS)**.
- La base de datos PostgreSQL de este microservicio corre en la **tercera
  MV** (privada, no pública), junto con MySQL (Clientes) y MongoDB (Envíos).

### Importante sobre docker compose y múltiples VMs

`docker compose` por sí solo **no orquesta múltiples hosts**: cada VM corre
su propio `docker compose up` de forma independiente. Esto tiene dos
implicaciones que hay que resolver de forma consistente en los 3
microservicios con base de datos (Clientes, Vehículos, Envíos):

1. **Conexión a la base de datos**: en local, `DB_HOST` apunta al nombre del
   contenedor de BD (`db-vehiculos`, `mysql-clientes`, etc.) porque están en
   la misma red de docker compose. En producción, la BD vive en una VM
   distinta a la del microservicio, así que `DB_HOST` debe sobreescribirse
   con la **IP privada de la 3ra VM** vía variable de entorno (ya está
   parametrizado para esto, solo falta setearlo al desplegar).
2. **Llamadas entre microservicios** (ej. Envíos llamando a
   `GET /clientes/{id}/direcciones` o `PATCH /vehiculos/{id}/estado`): no se
   pueden resolver por nombre de contenedor entre VMs distintas. Cada
   microservicio consumidor debe tener la URL base de los microservicios que
   consume como variable de entorno (IP privada de la VM + puerto, detrás
   del balanceador si aplica), nunca hardcodeada.

## Próximos pasos sugeridos

- Tests unitarios/de integración (servicios + repositorios con Testcontainers)
- Autenticación entre microservicios (API Gateway + JWT/mTLS interno)
- Endpoint de reporte de disponibilidad de flota por tipo/estado para el
  microservicio agregador
