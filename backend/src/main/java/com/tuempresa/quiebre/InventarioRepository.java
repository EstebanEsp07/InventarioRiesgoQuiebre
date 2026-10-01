package com.restaurante.quiebre.inventario;

import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class InventarioRepository {
    private final JdbcClient jdbc;

    public InventarioRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<ExistenciaVista> listarExistencias() {
        return jdbc.sql("""
                SELECT e.bodega_id, b.nombre AS bodega, e.producto_id, p.sku, p.nombre AS producto,
                       p.unidad, e.cantidad, p.lead_time_dias
                FROM inventario.existencia e
                JOIN inventario.bodega b ON b.id = e.bodega_id
                JOIN inventario.producto p ON p.id = e.producto_id
                ORDER BY b.nombre, p.nombre""")
                .query(ExistenciaVista.class).list();
    }

    public List<Double> consumoDiario(long bodegaId, long productoId, int dias) {
        return jdbc.sql("""
                SELECT cantidad FROM inventario.consumo_diario
                WHERE bodega_id = :b AND producto_id = :p AND fecha >= current_date - :d
                ORDER BY fecha""")
                .param("b", bodegaId).param("p", productoId).param("d", dias)
                .query(Double.class).list();
    }

    /** Transferencia atómica: descuenta en origen (si alcanza) y suma en destino. */
    public void mover(long origenId, long destinoId, long productoId, double cantidad) {
        int n = jdbc.sql("""
                UPDATE inventario.existencia SET cantidad = cantidad - :c
                WHERE bodega_id = :o AND producto_id = :p AND cantidad >= :c""")
                .param("c", cantidad).param("o", origenId).param("p", productoId).update();
        if (n == 0) {
            throw new IllegalStateException("Existencia insuficiente en la bodega de origen");
        }
        jdbc.sql("""
                INSERT INTO inventario.existencia (bodega_id, producto_id, cantidad)
                VALUES (:d, :p, :c)
                ON CONFLICT (bodega_id, producto_id)
                DO UPDATE SET cantidad = inventario.existencia.cantidad + EXCLUDED.cantidad""")
                .param("d", destinoId).param("p", productoId).param("c", cantidad).update();
    }
}
