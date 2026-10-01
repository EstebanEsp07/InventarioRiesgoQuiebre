package com.restaurante.quiebre.integracion;

public record OutboxMensaje(long id, String tipo, String payload) {}
