-- ============================================================
-- Script de creación de schema para svc-vehiculos (PostgreSQL)
-- Debe correrse UNA VEZ contra la base "vehicles_db" en la VM
-- de bases de datos (VM3), ANTES de levantar el microservicio
-- con ddl-auto: validate.
--
-- Refleja exactamente las entidades Vehiculo.java y Conductor.java.
-- ============================================================

-- ---------- Secuencias ----------
-- allocationSize=50 en las entidades Java => la secuencia debe
-- incrementar de 50 en 50 para que Hibernate y Postgres no se
-- desincronicen en el conteo de IDs.
CREATE SEQUENCE IF NOT EXISTS vehiculos_id_vehiculo_seq
    START WITH 1
    INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS conductores_id_conductor_seq
    START WITH 1
    INCREMENT BY 50;

-- ---------- Tabla: vehiculos ----------
CREATE TABLE IF NOT EXISTS vehiculos (
    id_vehiculo        BIGINT PRIMARY KEY DEFAULT nextval('vehiculos_id_vehiculo_seq'),
    placa               VARCHAR(10)     NOT NULL UNIQUE,
    tipo                 VARCHAR(20)     NOT NULL,   -- MOTO | FURGONETA | CAMION
    capacidad_kg        NUMERIC(10,2)   NOT NULL,
    marca                VARCHAR(50),
    modelo               VARCHAR(50),
    anio_fabricacion    INTEGER,
    estado               VARCHAR(20)     NOT NULL DEFAULT 'DISPONIBLE', -- DISPONIBLE | EN_RUTA | MANTENIMIENTO | INACTIVO
    fecha_registro      TIMESTAMP       NOT NULL DEFAULT now()
);

ALTER SEQUENCE vehiculos_id_vehiculo_seq OWNED BY vehiculos.id_vehiculo;

-- ---------- Tabla: conductores ----------
CREATE TABLE IF NOT EXISTS conductores (
    id_conductor        BIGINT PRIMARY KEY DEFAULT nextval('conductores_id_conductor_seq'),
    id_vehiculo         BIGINT,
    nombre               VARCHAR(80)     NOT NULL,
    apellido             VARCHAR(80)     NOT NULL,
    nro_licencia        VARCHAR(20)     NOT NULL UNIQUE,
    dni                  VARCHAR(15)     NOT NULL UNIQUE,
    telefono             VARCHAR(20),
    turno                VARCHAR(10)     NOT NULL,   -- MANANA | TARDE | NOCHE
    fecha_contratacion  DATE,
    activo               BOOLEAN         NOT NULL DEFAULT true,

    CONSTRAINT fk_conductor_vehiculo
        FOREIGN KEY (id_vehiculo)
        REFERENCES vehiculos (id_vehiculo)
        ON DELETE SET NULL   -- coincide con @OnDelete(SET_NULL) en Conductor.java
);

ALTER SEQUENCE conductores_id_conductor_seq OWNED BY conductores.id_conductor;

-- ---------- Índices recomendados ----------
-- FKs no indexan automáticamente en Postgres; esto acelera
-- listarConductoresDeVehiculo() y findByVehiculoId().
CREATE INDEX IF NOT EXISTS idx_conductores_id_vehiculo ON conductores (id_vehiculo);
CREATE INDEX IF NOT EXISTS idx_conductores_turno ON conductores (turno);
CREATE INDEX IF NOT EXISTS idx_vehiculos_tipo ON vehiculos (tipo);
CREATE INDEX IF NOT EXISTS idx_vehiculos_estado ON vehiculos (estado);
