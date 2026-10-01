package com.restaurante.quiebre.inventario;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/existencias")
public class InventarioController {
    private final InventarioRepository repo;

    public InventarioController(InventarioRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<ExistenciaVista> listar() {
        return repo.listarExistencias();
    }
}
