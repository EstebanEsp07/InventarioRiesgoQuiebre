CREATE SCHEMA IF NOT EXISTS inventario;
CREATE SCHEMA IF NOT EXISTS recomendaciones;
CREATE SCHEMA IF NOT EXISTS aprobacion;
CREATE SCHEMA IF NOT EXISTS auditoria;
CREATE SCHEMA IF NOT EXISTS integracion;

-- Módulo inventario
CREATE TABLE inventario.bodega (
  id BIGSERIAL PRIMARY KEY,
  nombre TEXT NOT NULL UNIQUE
);
CREATE TABLE inventario.producto (
  id BIGSERIAL PRIMARY KEY,
  sku TEXT NOT NULL UNIQUE,
  nombre TEXT NOT NULL,
  unidad TEXT NOT NULL,
  lead_time_dias INT NOT NULL DEFAULT 2
);
CREATE TABLE inventario.existencia (
  bodega_id BIGINT NOT NULL REFERENCES inventario.bodega(id),
  producto_id BIGINT NOT NULL REFERENCES inventario.producto(id),
  cantidad NUMERIC(12,2) NOT NULL CHECK (cantidad >= 0),
  PRIMARY KEY (bodega_id, producto_id)
);
CREATE TABLE inventario.consumo_diario (
  bodega_id BIGINT NOT NULL REFERENCES inventario.bodega(id),
  producto_id BIGINT NOT NULL REFERENCES inventario.producto(id),
  fecha DATE NOT NULL,
  cantidad NUMERIC(12,2) NOT NULL,
  PRIMARY KEY (bodega_id, producto_id, fecha)
);

-- Módulo recomendaciones (sin FKs entre esquemas: se guarda snapshot de nombres)
CREATE TABLE recomendaciones.recomendacion (
  id BIGSERIAL PRIMARY KEY,
  tipo TEXT NOT NULL CHECK (tipo IN ('TRANSFERENCIA','COMPRA')),
  producto_id BIGINT NOT NULL,
  producto_nombre TEXT NOT NULL,
  bodega_destino_id BIGINT NOT NULL,
  bodega_destino_nombre TEXT NOT NULL,
  bodega_origen_id BIGINT,
  bodega_origen_nombre TEXT,
  cantidad NUMERIC(12,2) NOT NULL,
  dias_cobertura NUMERIC(8,2) NOT NULL,
  riesgo TEXT NOT NULL CHECK (riesgo IN ('ALTO','MEDIO')),
  estimacion_degradada BOOLEAN NOT NULL DEFAULT FALSE,
  estado TEXT NOT NULL DEFAULT 'PENDIENTE'
    CHECK (estado IN ('PENDIENTE','APROBADA','RECHAZADA','EXPIRADA','EJECUTADA')),
  creada_en TIMESTAMPTZ NOT NULL DEFAULT now(),
  decidida_en TIMESTAMPTZ,
  decidida_por TEXT,
  motivo TEXT,
  orden_externa_id TEXT
);
-- Evita recomendaciones pendientes duplicadas para el mismo producto/bodega
CREATE UNIQUE INDEX uq_recomendacion_pendiente
  ON recomendaciones.recomendacion (bodega_destino_id, producto_id) WHERE estado = 'PENDIENTE';

-- Módulo aprobación
CREATE TABLE aprobacion.decision (
  id BIGSERIAL PRIMARY KEY,
  recomendacion_id BIGINT NOT NULL,
  decision TEXT NOT NULL CHECK (decision IN ('APROBADA','RECHAZADA')),
  usuario TEXT NOT NULL,
  motivo TEXT,
  decidida_en TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Módulo auditoría (solo inserciones)
CREATE TABLE auditoria.evento (
  id BIGSERIAL PRIMARY KEY,
  ocurrido_en TIMESTAMPTZ NOT NULL DEFAULT now(),
  actor TEXT NOT NULL,
  accion TEXT NOT NULL,
  entidad TEXT NOT NULL,
  entidad_id TEXT NOT NULL,
  detalle TEXT
);

-- Outbox: garantiza que el evento se publique aunque RabbitMQ esté caído
CREATE TABLE integracion.outbox (
  id BIGSERIAL PRIMARY KEY,
  tipo TEXT NOT NULL,
  payload TEXT NOT NULL,
  creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
  publicado_en TIMESTAMPTZ
);
CREATE INDEX ix_outbox_pendiente ON integracion.outbox (id) WHERE publicado_en IS NULL;
