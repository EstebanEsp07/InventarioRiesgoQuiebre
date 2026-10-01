package com.restaurante.quiebre.pronostico;

/** Demanda diaria estimada. {@code degradada=true} indica que se usó el fallback local. */
public record Demanda(double diaria, String modelo, boolean degradada) {}
