/**
 * InstrumentoController.java
 * Controlador REST para gestionar operaciones CRUD de instrumentos.
 * - Crear, actualizar, eliminar y consultar instrumentos.
 * - Consultar instrumentos por categoría.
 */
package com.utn.frm.instrumentos.controllers;

import com.utn.frm.instrumentos.entities.Categoria;
import com.utn.frm.instrumentos.entities.Instrumento;
import com.utn.frm.instrumentos.repositories.CategoriaRepository;
import com.utn.frm.instrumentos.repositories.InstrumentoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/instrumentos")
public class InstrumentoController {

    private final InstrumentoRepository instrumentoRepo;
    private final CategoriaRepository categoriaRepo;

    public InstrumentoController(InstrumentoRepository instrumento, CategoriaRepository categoria) {
        this.instrumentoRepo = instrumento;
        this.categoriaRepo = categoria;
    }

    /**
     * Obtiene todos los instrumentos registrados.
     * @return Lista de Instrumento.
     */
    @GetMapping("/all")
    public List<Instrumento> getAll() {
        return instrumentoRepo.findAll();
    }

    /**
     * Obtiene instrumentos filtrados por categoría.
     * @param id ID de la categoría.
     * @return Lista de Instrumento asociados a la categoría.
     */
    @GetMapping("/categoria/{id}")
    public List<Instrumento> getByCategoria(@PathVariable Long id) {
        Categoria categoria = categoriaRepo.findById(id).orElse(null);
        return instrumentoRepo.findByCategoria(categoria);
    }

    /**
     * Busca un instrumento por su ID.
     * @param id ID del instrumento.
     * @return ResponseEntity con el Instrumento encontrado o error 404.
     */
    @GetMapping("/id/{id}")
    public ResponseEntity<Instrumento> getInstrumento(@PathVariable Long id) {
        Optional<Instrumento> instrumento = instrumentoRepo.findById(id);
        return instrumento.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Crea un nuevo instrumento.
     * @param instrumento Datos del instrumento a crear (incluye categoría).
     * @return Instrumento creado.
     * @throws RuntimeException Si la categoría asociada no existe.
     */
    @PostMapping("/crear")
    public Instrumento crear(@RequestBody Instrumento instrumento) {
        Optional<Categoria> categoria = categoriaRepo.findById(instrumento.getCategoria().getId());
        if (categoria.isEmpty()) {
            throw new RuntimeException("Categoría no encontrada");
        }
        instrumento.setCategoria(categoria.get());
        return instrumentoRepo.save(instrumento);
    }

    /**
     * Actualiza un instrumento existente.
     * @param id ID del instrumento a actualizar.
     * @param instrumento Nuevos datos del instrumento.
     * @return Instrumento actualizado.
     * @throws RuntimeException Si el instrumento o categoría no existen.
     */
    @PutMapping("/actualizar/{id}")
    public Instrumento actualizar(
            @PathVariable Long id,
            @RequestBody Instrumento instrumentoActualizado) {  // Renombrar a "instrumentoActualizado" para claridad

        // 1. Buscar el instrumento existente
        Instrumento instrumentoExistente = instrumentoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Instrumento no encontrado"));

        // 2. Validar que la categoría no sea nula
        if (instrumentoActualizado.getCategoria() == null) {
            throw new RuntimeException("La categoría es obligatoria");
        }

        // 3. Buscar la categoría en la base de datos
        Categoria categoria = categoriaRepo.findById(instrumentoActualizado.getCategoria().getId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // 4. Copiar campos del DTO al instrumento existente (actualizar solo campos permitidos)
        instrumentoExistente.setMarca(instrumentoActualizado.getMarca());
        instrumentoExistente.setModelo(instrumentoActualizado.getModelo());
        instrumentoExistente.setImagen(instrumentoActualizado.getImagen());
        instrumentoExistente.setPrecio(instrumentoActualizado.getPrecio());
        instrumentoExistente.setCostoEnvio(instrumentoActualizado.getCostoEnvio());
        instrumentoExistente.setCantidadVendida(instrumentoActualizado.getCantidadVendida());
        instrumentoExistente.setDescripcion(instrumentoActualizado.getDescripcion());
        instrumentoExistente.setCategoria(categoria);

        // 5. Guardar cambios
        return instrumentoRepo.save(instrumentoExistente);
    }

    /**
     * Elimina un instrumento por su ID.
     * @param id ID del instrumento a eliminar.
     * @return Mensaje de confirmación.
     * @throws RuntimeException Si el instrumento no existe.
     */
    @DeleteMapping("/borrar/{id}")
    public String delete(@PathVariable Long id) {
        if (!instrumentoRepo.existsById(id)) {
            throw new RuntimeException("Instrumento no encontrado");
        }
        instrumentoRepo.deleteById(id);
        return "Instrumento eliminado con éxito";
    }
}