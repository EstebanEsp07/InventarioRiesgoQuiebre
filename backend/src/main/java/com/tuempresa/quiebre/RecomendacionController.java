package com.restaurante.quiebre.recomendaciones;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/recomendaciones")
public class RecomendacionController {
    private final RecomendacionRepository repo;
    private final RecomendacionService service;

    public RecomendacionController(RecomendacionRepository repo, RecomendacionService service) {
        this.repo = repo;
        this.service = service;
    }

    @GetMapping
    public List<Recomendacion> listar(@RequestParam(required = false) String estado) {
        return repo.listar(estado);
    }

    @GetMapping("/{id}")
    public Recomendacion obtener(@PathVariable long id) {
        return repo.buscar(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe"));
    }

    @PostMapping("/evaluar")
    public Map<String, Integer> evaluar() {
        service.expirarVencidas();
        return Map.of("creadas", service.evaluar());
    }
}
