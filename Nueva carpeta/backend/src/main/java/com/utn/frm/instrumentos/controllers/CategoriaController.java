package com.utn.frm.instrumentos.controllers;

import com.utn.frm.instrumentos.entities.Categoria;
import com.utn.frm.instrumentos.repositories.CategoriaRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")  // Habilita CORS solo para este controlador
@RestController
@RequestMapping("/api/categoria")
public class CategoriaController {

    private final CategoriaRepository repo;

    public CategoriaController(CategoriaRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Categoria> listarCategorias() {
        return repo.findAll();
    }
}
