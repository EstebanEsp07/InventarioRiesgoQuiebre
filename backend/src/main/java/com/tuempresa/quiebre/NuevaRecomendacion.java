package com.restaurante.quiebre.recomendaciones;

public record NuevaRecomendacion(String tipo, long productoId, String productoNombre,
                                 long destinoId, String destinoNombre,
                                 Long origenId, String origenNombre,
                                 double cantidad, double diasCobertura, String riesgo,
                                 boolean degradada) {}
