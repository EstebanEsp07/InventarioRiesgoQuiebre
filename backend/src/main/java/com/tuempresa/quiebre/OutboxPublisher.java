package com.restaurante.quiebre.integracion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Publica los eventos de la tabla outbox en RabbitMQ (entrega al menos una vez). */
@Component
public class OutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private final OutboxRepository outbox;
    private final RabbitTemplate rabbit;

    public OutboxPublisher(OutboxRepository outbox, RabbitTemplate rabbit) {
        this.outbox = outbox;
        this.rabbit = rabbit;
    }

    @Scheduled(fixedDelay = 2000)
    public void publicar() {
        for (var m : outbox.pendientes(50)) {
            try {
                rabbit.convertAndSend(RabbitConfig.EXCHANGE, m.tipo(), m.payload());
                outbox.marcarPublicado(m.id());
            } catch (AmqpException e) {
                log.warn("RabbitMQ no disponible, se reintentará: {}", e.getMessage());
                return;
            }
        }
    }
}
