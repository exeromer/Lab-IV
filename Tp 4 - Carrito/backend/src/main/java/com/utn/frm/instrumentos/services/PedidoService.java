package com.utn.frm.instrumentos.services;

import com.utn.frm.instrumentos.dto.InstrumentoDTO;
import com.utn.frm.instrumentos.dto.PedidoDTO;
import com.utn.frm.instrumentos.dto.PedidoDetalleDTO;
import com.utn.frm.instrumentos.entities.Instrumento;
import com.utn.frm.instrumentos.entities.Pedido;
import com.utn.frm.instrumentos.entities.PedidoDetalle;
import com.utn.frm.instrumentos.repositories.InstrumentoRepository;
import com.utn.frm.instrumentos.repositories.PedidoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private InstrumentoRepository instrumentoRepository;

    // Método para crear un pedido desde el DTO
    @Transactional
    public Pedido crearPedido(PedidoDTO pedidoDTO) {
        Pedido pedido = new Pedido();
        pedido.setFecha(pedidoDTO.getFecha());
        pedido.setTotal(BigDecimal.ZERO);

        List<PedidoDetalle> detalles = new ArrayList<>();
        for (PedidoDetalleDTO detalleDTO : pedidoDTO.getDetalles()) {
            PedidoDetalle detalle = new PedidoDetalle();
            detalle.setCantidad(detalleDTO.getCantidad());
            detalle.setPrecioUnitario(detalleDTO.getPrecioUnitario());

            // Obtener el ID del instrumento directamente desde el DTO
            Instrumento instrumento = instrumentoRepository.findById(detalleDTO.getInstrumentoId())
                    .orElseThrow(() -> new RuntimeException("Instrumento no encontrado con ID: " + detalleDTO.getInstrumentoId()));

            detalle.setInstrumento(instrumento);
            detalle.setPedido(pedido);
            detalles.add(detalle);
        }

        BigDecimal total = calcularTotal(detalles);
        pedido.setTotal(total);
        pedido.setDetalles(detalles);

        return pedidoRepository.save(pedido);
    }

    // Método para obtener todos los pedidos como DTOs
    public List<PedidoDTO> obtenerTodosPedidos() {
        return pedidoRepository.findAll().stream()
                .map(this::convertirAPedidoDTO)
                .collect(Collectors.toList());
    }

    // Métodos de conversión
    private PedidoDTO convertirAPedidoDTO(Pedido pedido) {
        PedidoDTO dto = new PedidoDTO();
        dto.setId(pedido.getId());
        dto.setFecha(pedido.getFecha());
        dto.setTotal(pedido.getTotal());
        dto.setDetalles(convertirDetallesAPedidoDetalleDTO(pedido.getDetalles()));
        return dto;
    }

    private List<PedidoDetalleDTO> convertirDetallesAPedidoDetalleDTO(List<PedidoDetalle> detalles) {
        return detalles.stream()
                .map(this::convertirAPedidoDetalleDTO)
                .collect(Collectors.toList());
    }

    private PedidoDetalleDTO convertirAPedidoDetalleDTO(PedidoDetalle detalle) {
        PedidoDetalleDTO dto = new PedidoDetalleDTO();
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioUnitario(detalle.getPrecioUnitario());
        dto.setInstrumentoId(detalle.getInstrumento().getId());
        dto.setNombreInstrumento(detalle.getInstrumento().getInstrumento()); // Copiar el nombre del instrumento
        return dto;
    }

    private BigDecimal calcularTotal(List<PedidoDetalle> detalles) {
        return detalles.stream()
                .map(d -> d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}