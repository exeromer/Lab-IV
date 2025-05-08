package com.utn.frm.instrumentos.controllers;

import com.utn.frm.instrumentos.dto.PedidoDTO;
import com.utn.frm.instrumentos.entities.Pedido;
import com.utn.frm.instrumentos.services.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/pedido")
@CrossOrigin(origins = "*")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<?> crearPedido(@RequestBody PedidoDTO pedidoDTO) {
        try {
            Pedido pedidoGuardado = pedidoService.crearPedido(pedidoDTO);
            return ResponseEntity.ok().body(
                    Collections.singletonMap("mensaje", "El pedido con id " + pedidoGuardado.getId() + " se guardó correctamente")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Collections.singletonMap("error", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Collections.singletonMap("error", "Error interno del servidor")
            );
        }
    }

    @GetMapping
    public ResponseEntity<List<PedidoDTO>> obtenerTodosPedidos() {
        List<PedidoDTO> pedidos = pedidoService.obtenerTodosPedidos();
        return ResponseEntity.ok(pedidos);
    }
}
