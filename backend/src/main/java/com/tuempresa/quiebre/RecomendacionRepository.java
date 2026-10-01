package com.restaurante.quiebre.recomendaciones;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class RecomendacionRepository {
    private final JdbcClient jdbc;

    public RecomendacionRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public long crear(NuevaRecomendacion n) {
        return jdbc.sql("""
                INSERT INTO recomendaciones.recomendacion
                  (tipo, producto_id, producto_nombre, bodega_destino_id, bodega_destino_nombre,
                   bodega_origen_id, bodega_origen_nombre, cantidad, dias_cobertura, riesgo, estimacion_degradada)
                VALUES (:tipo, :productoId, :productoNombre, :destinoId, :destinoNombre,
                        :origenId, :origenNombre, :cantidad, :dias, :riesgo, :degradada)
                RETURNING id""")
                .param("tipo", n.tipo()).param("productoId", n.productoId())
                .param("productoNombre", n.productoNombre())
                .param("destinoId", n.destinoId()).param("destinoNombre", n.destinoNombre())
                .param("origenId", n.origenId()).param("origenNombre", n.origenNombre())
                .param("cantidad", n.cantidad()).param("dias", n.diasCobertura())
                .param("riesgo", n.riesgo()).param("degradada", n.degradada())
                .query(Long.class).single();
    }

    public List<Recomendacion> listar(String estado) {
        if (estado == null || estado.isBlank()) {
            return jdbc.sql("SELECT * FROM recomendaciones.recomendacion ORDER BY creada_en DESC, id DESC")
                    .query(Recomendacion.class).list();
        }
        return jdbc.sql("SELECT * FROM recomendaciones.recomendacion WHERE estado = :e ORDER BY creada_en DESC, id DESC")
                .param("e", estado).query(Recomendacion.class).list();
    }

    public Optional<Recomendacion> buscar(long id) {
        return jdbc.sql("SELECT * FROM recomendaciones.recomendacion WHERE id = :id")
                .param("id", id).query(Recomendacion.class).optional();
    }

    public boolean existePendiente(long bodegaId, long productoId) {
        Long n = jdbc.sql("""
                SELECT count(*) FROM recomendaciones.recomendacion
                WHERE bodega_destino_id = :b AND producto_id = :p AND estado = 'PENDIENTE'""")
                .param("b", bodegaId).param("p", productoId).query(Long.class).single();
        return n != null && n > 0;
    }

    /** Transición PENDIENTE -> APROBADA/RECHAZADA. Devuelve false si ya no estaba pendiente. */
    public boolean registrarDecision(long id, String estado, String usuario, String motivo) {
        return jdbc.sql("""
                UPDATE recomendaciones.recomendacion
                SET estado = :estado, decidida_en = now(), decidida_por = :usuario, motivo = :motivo
                WHERE id = :id AND estado = 'PENDIENTE'""")
                .param("estado", estado).param("usuario", usuario).param("motivo", motivo).param("id", id)
                .update() == 1;
    }

    public void marcarEjecutada(long id, String ordenExternaId) {
        jdbc.sql("UPDATE recomendaciones.recomendacion SET estado = 'EJECUTADA', orden_externa_id = :o WHERE id = :id")
                .param("o", ordenExternaId).param("id", id).update();
    }

    public int expirarVencidas(int horas) {
        return jdbc.sql("""
                UPDATE recomendaciones.recomendacion SET estado = 'EXPIRADA'
                WHERE estado = 'PENDIENTE' AND creada_en < now() - make_interval(hours => :h)""")
                .param("h", horas).update();
    }
}
