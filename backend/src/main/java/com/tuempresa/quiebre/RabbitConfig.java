package com.restaurante.quiebre.integracion;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "inventario.eventos";
    public static final String COMPRA_SOLICITADA = "compra.solicitada";
    public static final String QUEUE_COMPRAS = "compras.solicitudes";
    public static final String QUEUE_COMPRAS_DLQ = "compras.solicitudes.dlq";

    @Bean
    DirectExchange eventos() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue comprasQueue() {
        return QueueBuilder.durable(QUEUE_COMPRAS)
                .deadLetterExchange("")
                .deadLetterRoutingKey(QUEUE_COMPRAS_DLQ)
                .build();
    }

    @Bean
    Queue comprasDlq() {
        return QueueBuilder.durable(QUEUE_COMPRAS_DLQ).build();
    }

    @Bean
    Binding comprasBinding(Queue comprasQueue, DirectExchange eventos) {
        return BindingBuilder.bind(comprasQueue).to(eventos).with(COMPRA_SOLICITADA);
    }
}
