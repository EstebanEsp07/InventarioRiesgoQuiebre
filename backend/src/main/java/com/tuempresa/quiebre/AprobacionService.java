package com.restaurante.quiebre.aprobacion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurante.quiebre.auditoria.AuditoriaService;
import com.restaurante.quiebre.integracion.CompraSolicitada;
import com.restaurante.quiebre.integracion.OutboxRepository;
import com.restaurante.quiebre.integracion.RabbitConfig;
import com.restaurante.quiebre.inventario.InventarioRepository;
import com.restaurante.quiebre.recomendaciones.Recomendacion;
import com.restaurante.quiebre.recomendaciones.RecomendacionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Nada se ejecuta sin la decisión explícita de una persona. */
@Service
public class AprobacionService {
    private final RecomendacionRepository recomendaciones;
    private final InventarioRepository inventario;
    private final OutboxRepository outbox;
    private final AuditoriaService auditoria;
    private final JdbcClient jdbc;
    private final ObjectMapper mapper;

    public AprobacionService(RecomendacionRepository recomendaciones, InventarioRepository inventario,
                             OutboxRepository outbox, AuditoriaService auditoria, JdbcClient jdbc,
                             ObjectMapper mapper) {
        this.recomendaciones = recomendaciones;
        this.inventario = inventario;
        this.outbox = outbox;
        this.auditoria = auditoria;
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @Transactional
    public Recomendacion decidir(long id, boolean aprobar, String usuario, String motivo) {
        Recomendacion rec = recomendaciones.buscar(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recomendación inexistente"));

        String decision = aprobar ? "APROBADA" : "RECHAZADA";
        if (!recomendaciones.registrarDecision(id, decision, usuario, motivo)) {
            throw new IllegalStateException("La recomendación ya no está pendiente (estado: " + rec.estado() + ")");
        }
        jdbc.sql("INSERT INTO aprobacion.decision (recomendacion_id, decision, usuario, motivo) VALUES (:r, :d, :u, :m)")
                .param("r", id).param("d", decision).param("u", usuario).param("m", motivo).update();
        auditoria.registrar(usuario, "RECOMENDACION_" + decision, "recomendacion", id, motivo);

        if (aprobar) {
            if ("TRANSFERENCIA".equals(rec.tipo())) {
                inventario.mover(rec.bodegaOrigenId(), rec.bodegaDestinoId(), rec.productoId(), rec.cantidad());
                recomendaciones.marcarEjecutada(id, null);
                auditoria.registrar("sistema", "TRANSFERENCIA_EJECUTADA", "recomendacion", id,
                        "%s -> %s: %.0f".formatted(rec.bodegaOrigenNombre(), rec.bodegaDestinoNombre(), rec.cantidad()));
            } else {
                var evento = new CompraSolicitada(id, rec.productoId(), rec.productoNombre(),
                        rec.bodegaDestinoId(), rec.cantidad());
                // Se guarda en la misma transacción; OutboxPublisher lo enviará a RabbitMQ
                outbox.insertar(RabbitConfig.COMPRA_SOLICITADA, mapper.valueToTree(evento).toString());
            }
        }
        return recomendaciones.buscar(id).orElseThrow();
    }
}
