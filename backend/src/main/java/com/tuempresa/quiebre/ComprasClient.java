package com.restaurante.quiebre.integracion;

import com.restaurante.quiebre.config.AppProperties;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Adaptador hacia el sistema de compras existente (anti-corruption layer). */
@Component
public class ComprasClient {
    public record OrdenCompra(String ordenId, String estado) {}

    private final RestClient client;

    public ComprasClient(AppProperties props, RestClient.Builder builder) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        this.client = builder.baseUrl(props.compras().baseUrl())
                .defaultHeader("X-API-Key", props.compras().apiKey())
                .requestFactory(factory)
                .build();
    }

    public OrdenCompra crearOrden(CompraSolicitada c) {
        return client.post().uri("/api/ordenes-compra")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "referencia", "REC-" + c.recomendacionId(),   // idempotencia en el sistema externo
                        "productoId", c.productoId(),
                        "cantidad", c.cantidad(),
                        "bodegaDestinoId", c.bodegaDestinoId()))
                .retrieve()
                .body(OrdenCompra.class);
    }
}
