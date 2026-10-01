package com.restaurante.quiebre.metricas;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metricas")
public class MetricasController {
    private final JdbcClient jdbc;

    public MetricasController(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    public Map<String, Object> metricas() {
        var m = new LinkedHashMap<String, Object>();

        // Quiebres actuales (existencia = 0) por producto
        m.put("quiebresPorProducto", jdbc.sql("""
                SELECT p.nombre AS producto, count(*) AS bodegas
                FROM inventario.existencia e JOIN inventario.producto p ON p.id = e.producto_id
                WHERE e.cantidad <= 0 GROUP BY p.nombre ORDER BY p.nombre""").query().listOfRows());

        // Productos con riesgo ALTO pendientes de decisión
        m.put("riesgoAltoPendientePorProducto", jdbc.sql("""
                SELECT producto_nombre AS producto, count(*) AS bodegas
                FROM recomendaciones.recomendacion
                WHERE estado = 'PENDIENTE' AND riesgo = 'ALTO'
                GROUP BY producto_nombre ORDER BY producto_nombre""").query().listOfRows());

        m.put("tiempoMedioAprobacionMinutos", jdbc.sql("""
                SELECT COALESCE(EXTRACT(EPOCH FROM AVG(decidida_en - creada_en)) / 60, 0)
                FROM recomendaciones.recomendacion WHERE decidida_en IS NOT NULL""")
                .query(Double.class).single());

        m.put("comprasUrgentes", jdbc.sql("""
                SELECT count(*) FROM recomendaciones.recomendacion
                WHERE tipo = 'COMPRA' AND riesgo = 'ALTO' AND estado IN ('APROBADA','EJECUTADA')""")
                .query(Long.class).single());

        long aceptadas = jdbc.sql("SELECT count(*) FROM recomendaciones.recomendacion WHERE estado IN ('APROBADA','EJECUTADA')")
                .query(Long.class).single();
        long decididas = jdbc.sql("SELECT count(*) FROM recomendaciones.recomendacion WHERE estado IN ('APROBADA','EJECUTADA','RECHAZADA')")
                .query(Long.class).single();
        m.put("recomendacionesAceptadas", aceptadas);
        m.put("recomendacionesDecididas", decididas);
        m.put("tasaAceptacion", decididas == 0 ? 0.0 : (double) aceptadas / decididas);
        m.put("recomendacionesPendientes", jdbc.sql("SELECT count(*) FROM recomendaciones.recomendacion WHERE estado = 'PENDIENTE'")
                .query(Long.class).single());
        return m;
    }
}
