package com.restaurante.quiebre.integracion;

/** Evento publicado cuando una persona aprueba una recomendación de compra. */
public record CompraSolicitada(long recomendacionId, long productoId, String producto,
                               long bodegaDestinoId, double cantidad) {}
