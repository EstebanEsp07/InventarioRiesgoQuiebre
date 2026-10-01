package com.restaurante.quiebre.recomendaciones;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class EvaluacionProgramada {
    private static final Logger log = LoggerFactory.getLogger(EvaluacionProgramada.class);
    private final RecomendacionService service;

    EvaluacionProgramada(RecomendacionService service) {
        this.service = service;
    }

    @Scheduled(fixedDelayString = "${app.evaluacion.intervalo-ms}", initialDelay = 30000)
    void ejecutar() {
        try {
            service.expirarVencidas();
            int n = service.evaluar();
            log.info("Evaluación periódica: {} recomendaciones nuevas", n);
        } catch (Exception e) {
            log.error("Falló la evaluación periódica", e);
        }
    }
}
