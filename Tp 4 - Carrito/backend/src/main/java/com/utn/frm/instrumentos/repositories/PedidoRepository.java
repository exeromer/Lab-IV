package com.utn.frm.instrumentos.repositories;

import com.utn.frm.instrumentos.entities.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Query("SELECT p FROM Pedido p LEFT JOIN FETCH p.detalles d LEFT JOIN FETCH d.instrumento")
    List<Pedido> findAllWithDetalles();

    @Query("SELECT p FROM Pedido p LEFT JOIN FETCH p.detalles d LEFT JOIN FETCH d.instrumento WHERE p.id = :id")
    Optional<Pedido> findByIdWithDetallesAndInstrumentos(@Param("id") Long id);

}