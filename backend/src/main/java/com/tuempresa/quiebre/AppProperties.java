package com.restaurante.quiebre.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Riesgo riesgo, Vigencia vigencia, Evaluacion evaluacion,
                            Forecast forecast, Compras compras) {
    public record Riesgo(int umbralDias) {}
    public record Vigencia(int expiraHoras) {}
    public record Evaluacion(long intervaloMs) {}
    public record Forecast(String url, int timeoutMs, int fallbackWindowDays) {}
    public record Compras(String baseUrl, String apiKey) {}
}
