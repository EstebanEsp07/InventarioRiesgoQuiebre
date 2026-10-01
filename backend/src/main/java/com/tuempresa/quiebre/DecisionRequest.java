package com.restaurante.quiebre.aprobacion;

import jakarta.validation.constraints.NotBlank;

public record DecisionRequest(@NotBlank String usuario, String motivo) {}
