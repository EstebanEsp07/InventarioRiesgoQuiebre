package com.restaurante.quiebre.recomendaciones;

import java.time.OffsetDateTime;

public record Recomendacion(long id, String tipo, long productoId, String productoNombre,
                            long bodegaDestinoId, String bodegaDestinoNombre,
                            Long bodegaOrigenId, String bodegaOrigenNombre,
                            double cantidad, double diasCobertura, String riesgo,
                            boolean estimacionDegradada, String estado,
                            OffsetDateTime creadaEn, OffsetDateTime decididaEn,
                            String decididaPor, String motivo, String ordenExternaId) {}
