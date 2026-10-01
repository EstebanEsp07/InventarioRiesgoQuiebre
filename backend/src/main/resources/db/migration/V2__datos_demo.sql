-- Datos de demostración (eliminar en producción)
INSERT INTO inventario.bodega (nombre) VALUES ('Bodega Central'), ('Sucursal Norte'), ('Sucursal Sur');
INSERT INTO inventario.producto (sku, nombre, unidad, lead_time_dias) VALUES
  ('ACE-001', 'Aceite de oliva', 'L', 2),
  ('HAR-002', 'Harina', 'kg', 3),
  ('POL-003', 'Pollo', 'kg', 1),
  ('TOM-004', 'Tomate', 'kg', 1);

INSERT INTO inventario.existencia (bodega_id, producto_id, cantidad)
SELECT b.id, p.id, v.cantidad
FROM (VALUES
  ('Bodega Central','ACE-001',12),  ('Bodega Central','HAR-002',100), ('Bodega Central','POL-003',90),  ('Bodega Central','TOM-004',500),
  ('Sucursal Norte','ACE-001',400), ('Sucursal Norte','HAR-002',30),  ('Sucursal Norte','POL-003',300), ('Sucursal Norte','TOM-004',60),
  ('Sucursal Sur','ACE-001',150),   ('Sucursal Sur','HAR-002',35),    ('Sucursal Sur','POL-003',20),    ('Sucursal Sur','TOM-004',700)
) AS v(bodega, sku, cantidad)
JOIN inventario.bodega b ON b.nombre = v.bodega
JOIN inventario.producto p ON p.sku = v.sku;

INSERT INTO inventario.consumo_diario (bodega_id, producto_id, fecha, cantidad)
SELECT b.id, p.id, current_date - g, 5 * p.id + 2 * b.id + (g % 3)
FROM inventario.bodega b
CROSS JOIN inventario.producto p
CROSS JOIN generate_series(1, 14) AS g;
