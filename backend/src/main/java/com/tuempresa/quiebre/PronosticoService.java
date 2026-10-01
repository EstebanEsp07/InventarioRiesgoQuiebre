package com.restaurante.quiebre.pronostico;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.restaurante.quiebre.config.AppProperties;
import com.restaurante.quiebre.inventario.InventarioRepository;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Consulta el servicio de pronóstico. Si no responde a tiempo o falla, calcula un
 * promedio móvil local y marca la estimación como degradada: el sistema nunca se detiene.
 */
@Service
public class PronosticoService {
    private static final Logger log = LoggerFactory.getLogger(PronosticoService.class);

    private final RestClient client;
    private final InventarioRepository inventario;
    private final int ventanaDias;

    public PronosticoService(AppProperties props, InventarioRepository inventario, RestClient.Builder builder) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(props.forecast().timeoutMs());
        factory.setReadTimeout(props.forecast().timeoutMs());
        this.client = builder.baseUrl(props.forecast().url()).requestFactory(factory).build();
        this.inventario = inventario;
        this.ventanaDias = props.forecast().fallbackWindowDays();
    }

    public Demanda estimar(long bodegaId, long productoId) {
        List<Double> historico = inventario.consumoDiario(bodegaId, productoId, ventanaDias);
        try {
            ForecastResponse resp = client.post().uri("/forecast")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "producto_id", String.valueOf(productoId),
                            "bodega_id", String.valueOf(bodegaId),
                            "consumo_diario_historico", historico,
                            "horizonte_dias", 7))
                    .retrieve()
                    .body(ForecastResponse.class);
            if (resp == null) {
                throw new IllegalStateException("Respuesta vacía del servicio de pronóstico");
            }
            return new Demanda(resp.demandaDiaria(), resp.modelo(), false);
        } catch (Exception e) {
            log.warn("Pronóstico no disponible (bodega={}, producto={}): {}. Se usa promedio móvil.",
                    bodegaId, productoId, e.getMessage());
            double promedio = historico.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            return new Demanda(promedio, "fallback-promedio-movil", true);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ForecastResponse(@JsonProperty("demanda_diaria_estimada") double demandaDiaria, String modelo) {}
}
