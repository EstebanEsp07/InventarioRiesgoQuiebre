-- Un esquema por módulo del monolito modular: fronteras claras y
-- posibilidad de extraer un módulo a servicio propio más adelante.
CREATE SCHEMA IF NOT EXISTS inventario;
CREATE SCHEMA IF NOT EXISTS pronostico;
CREATE SCHEMA IF NOT EXISTS recomendaciones;
CREATE SCHEMA IF NOT EXISTS aprobacion;
CREATE SCHEMA IF NOT EXISTS auditoria;
CREATE SCHEMA IF NOT EXISTS integracion;   -- outbox y estado de compras
