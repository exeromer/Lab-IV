package com.utn.frm.instrumentos.services;

import com.utn.frm.instrumentos.dto.*;
import com.utn.frm.instrumentos.entities.*;
import com.utn.frm.instrumentos.repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private InstrumentoRepository instrumentoRepository;

    @Transactional
    public PedidoResponseDTO crearPedido(PedidoRequestDTO pedidoRequest) {
        Pedido pedido = new Pedido();
        pedido.setFecha(ZonedDateTime.now(ZoneId.of("America/Argentina/Buenos_Aires")));

        List<PedidoDetalle> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (DetallePedidoRequestDTO detalleRequest : pedidoRequest.getDetalles()) {
            Instrumento instrumento = instrumentoRepository.findById(detalleRequest.getInstrumentoId())
                    .orElseThrow(() -> new RuntimeException("Instrumento no encontrado: " + detalleRequest.getInstrumentoId()));

            PedidoDetalle detalle = new PedidoDetalle();
            detalle.setCantidad(detalleRequest.getCantidad());
            detalle.setPrecioUnitario(instrumento.getPrecio());
            detalle.setInstrumento(instrumento);
            detalle.setPedido(pedido);

            detalles.add(detalle);
            total = total.add(instrumento.getPrecio().multiply(BigDecimal.valueOf(detalleRequest.getCantidad())));
        }

        pedido.setDetalles(detalles);
        pedido.setTotal(total);
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        return convertirAResponseDTO(pedidoGuardado);
    }

    public List<PedidoResponseDTO> obtenerTodosPedidos() {
        return pedidoRepository.findAllWithDetalles().stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    private PedidoResponseDTO convertirAResponseDTO(Pedido pedido) {
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(pedido.getId());
        dto.setFecha(pedido.getFecha());
        dto.setTotal(pedido.getTotal());
        dto.setDetalles(convertirDetallesAResponseDTO(pedido.getDetalles()));
        return dto;
    }

    private List<DetallePedidoResponseDTO> convertirDetallesAResponseDTO(List<PedidoDetalle> detalles) {
        return detalles.stream()
                .map(this::convertirDetalleAResponseDTO)
                .collect(Collectors.toList());
    }

    private DetallePedidoResponseDTO convertirDetalleAResponseDTO(PedidoDetalle detalle) {
        DetallePedidoResponseDTO dto = new DetallePedidoResponseDTO();
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioUnitario(detalle.getPrecioUnitario());
        dto.setInstrumento(convertirInstrumentoADTO(detalle.getInstrumento()));
        return dto;
    }

    private InstrumentoDTO convertirInstrumentoADTO(Instrumento instrumento) {
        InstrumentoDTO dto = new InstrumentoDTO();
        dto.setId(instrumento.getId());
        dto.setInstrumento(instrumento.getInstrumento());
        dto.setPrecio(instrumento.getPrecio());
        return dto;
    }

    //Busca una entidad Pedido completa por su ID, incluyendo sus detalles e instrumentos.
    public Optional<Pedido> findEntidadById(Long pedidoId) {
        return pedidoRepository.findByIdWithDetallesAndInstrumentos(pedidoId);
    }

    //Obtiene un PedidoResponseDTO para un pedido específico por su ID.
    public PedidoResponseDTO obtenerPedidoDTOPorId(Long pedidoId) {
        Optional<Pedido> pedidoOptional = pedidoRepository.findByIdWithDetallesAndInstrumentos(pedidoId);
        if (!pedidoOptional.isPresent()) {
            throw new RuntimeException("Pedido no encontrado con ID: " + pedidoId);
        }
        return convertirAResponseDTO(pedidoOptional.get());
    }

}