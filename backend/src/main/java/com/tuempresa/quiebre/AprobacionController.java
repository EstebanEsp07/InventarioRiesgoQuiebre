package com.restaurante.quiebre.aprobacion;

import com.restaurante.quiebre.recomendaciones.Recomendacion;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recomendaciones/{id}")
public class AprobacionController {
    private final AprobacionService service;

    public AprobacionController(AprobacionService service) {
        this.service = service;
    }

    @PostMapping("/aprobar")
    public Recomendacion aprobar(@PathVariable long id, @Valid @RequestBody DecisionRequest req) {
        return service.decidir(id, true, req.usuario(), req.motivo());
    }

    @PostMapping("/rechazar")
    public Recomendacion rechazar(@PathVariable long id, @Valid @RequestBody DecisionRequest req) {
        return service.decidir(id, false, req.usuario(), req.motivo());
    }
}
