package com.utn.frm.instrumentos.controllers;

import com.utn.frm.instrumentos.entities.Categoria;
import com.utn.frm.instrumentos.entities.Instrumento;
import com.utn.frm.instrumentos.repositories.CategoriaRepository;
import com.utn.frm.instrumentos.repositories.InstrumentoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*")  // Habilita CORS solo para este controlador
@RestController
@RequestMapping("/api/instrumentos")
public class InstrumentoController {

    private final InstrumentoRepository instrumentoRepo;
    private final CategoriaRepository categoriaRepo;

    public InstrumentoController(InstrumentoRepository instrumento, CategoriaRepository categoria) {
        this.instrumentoRepo = instrumento;
        this.categoriaRepo = categoria;
    }

    //Obtener todos los instrumentos
    @GetMapping("/all")
    public List<Instrumento> getAll() {
        return instrumentoRepo.findAll();
    }

    //Obtener  los instrumentos por categoria
    @GetMapping("/categoria/{id}")
    public List<Instrumento> getByCategoria(@PathVariable Long id) {
        Categoria categoria = categoriaRepo.findById(id).orElse(null);
        return instrumentoRepo.findByCategoria(categoria);
    }

    //Obtener instrumento por Id
    @GetMapping("/id/{id}")
    public ResponseEntity<Instrumento> getInstrumento(@PathVariable Long id) {
        Optional<Instrumento> instrumento = instrumentoRepo.findById(id);
        if (instrumento.isPresent()) {
            return ResponseEntity.ok(instrumento.get());
        } else {
            return ResponseEntity.notFound().build();  // Si no se encuentra el instrumento
        }
    }

    // Crear un nuevo instrumento
    @PostMapping ("/crear")
    public Instrumento crear(@RequestBody Instrumento instrumento) {
        // Verifica que la categoría exista antes de asociarla al instrumento
        Optional<Categoria> categoria = categoriaRepo.findById(instrumento.getCategoria().getId());
        if (categoria.isPresent()) {
            instrumento.setCategoria(categoria.get());
            return instrumentoRepo.save(instrumento);
        } else {
            throw new RuntimeException("Categoría no encontrada");
        }
    }

    // Actualizar un instrumento existente
    @PutMapping("/actualizar/{id}")
    public Instrumento actualizar(@PathVariable Long id, @RequestBody Instrumento instrumento) {
        // Verifica si el instrumento existe
        Optional<Instrumento> existingInstrumento = instrumentoRepo.findById(id);
        if (existingInstrumento.isPresent()) {
            // Verifica que la categoría exista antes de asociarla
            Optional<Categoria> categoria = categoriaRepo.findById(instrumento.getCategoria().getId());
            if (categoria.isPresent()) {
                instrumento.setCategoria(categoria.get());
                instrumento.setId(id); // Asegúrate de que el id se mantenga igual
                return instrumentoRepo.save(instrumento);
            } else {
                throw new RuntimeException("Categoría no encontrada");
            }
        } else {
            throw new RuntimeException("Instrumento no encontrado");
        }
    }
    // Eliminar un instrumento por id
    @DeleteMapping("/borrar/{id}")
    public String delete(@PathVariable Long id) {
        Optional<Instrumento> instrumento = instrumentoRepo.findById(id);
        if (instrumento.isPresent()) {
            instrumentoRepo.deleteById(id);
            return "Instrumento eliminado con éxito";
        } else {
            throw new RuntimeException("Instrumento no encontrado");
        }
    }
}
