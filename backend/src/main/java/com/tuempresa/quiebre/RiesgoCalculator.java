package com.restaurante.quiebre.recomendaciones;

/** Lógica pura de riesgo: sin dependencias, fácil de probar. */
public final class RiesgoCalculator {
    public enum Nivel { ALTO, MEDIO, BAJO }

    private RiesgoCalculator() {}

    public static double diasCobertura(double cantidad, double demandaDiaria) {
        return demandaDiaria <= 0 ? Double.POSITIVE_INFINITY : cantidad / demandaDiaria;
    }

    /** ALTO: menos de {@code umbralDias}; MEDIO: menos del doble; BAJO: el resto. */
    public static Nivel nivel(double diasCobertura, int umbralDias) {
        if (diasCobertura < umbralDias) return Nivel.ALTO;
        if (diasCobertura < umbralDias * 2.0) return Nivel.MEDIO;
        return Nivel.BAJO;
    }
}
