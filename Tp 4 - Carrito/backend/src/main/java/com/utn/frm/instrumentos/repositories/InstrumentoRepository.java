package com.utn.frm.instrumentos.repositories;

import com.utn.frm.instrumentos.entities.Instrumento;
import com.utn.frm.instrumentos.entities.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstrumentoRepository extends JpaRepository<Instrumento, Long> {
    List<Instrumento> findByCategoria(Categoria categoria);
    List<Instrumento> findByCategoriaId(Long idCategoria);
}
