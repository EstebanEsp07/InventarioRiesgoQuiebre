package com.restaurante.quiebre.integracion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurante.quiebre.auditoria.AuditoriaService;
import com.restaurante.quiebre.recomendaciones.RecomendacionRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Consume compra.solicitada y crea la orden en el sistema de compras. Fallos -> reintentos -> DLQ. */
@Component
public class ComprasListener {
    private final ObjectMapper mapper;
    private final ComprasClient compras;
    private final RecomendacionRepository recomendaciones;
    private final AuditoriaService auditoria;

    public ComprasListener(ObjectMapper mapper, ComprasClient compras,
                           RecomendacionRepository recomendaciones, AuditoriaService auditoria) {
        this.mapper = mapper;
        this.compras = compras;
        this.recomendaciones = recomendaciones;
        this.auditoria = auditoria;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_COMPRAS)
    public void alSolicitarCompra(String payload) throws Exception {
        CompraSolicitada msg = mapper.readValue(payload, CompraSolicitada.class);
        var rec = recomendaciones.buscar(msg.recomendacionId())
                .orElseThrow(() -> new IllegalStateException("Recomendación inexistente: " + msg.recomendacionId()));
        if ("EJECUTADA".equals(rec.estado())) return;   // idempotencia ante reentregas

        var orden = compras.crearOrden(msg);
        recomendaciones.marcarEjecutada(rec.id(), orden.ordenId());
        auditoria.registrar("sistema", "COMPRA_EJECUTADA", "recomendacion", rec.id(),
                "Orden " + orden.ordenId() + " creada en el sistema de compras");
    }
}
