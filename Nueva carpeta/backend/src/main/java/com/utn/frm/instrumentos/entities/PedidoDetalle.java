package com.utn.frm.instrumentos.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pedido_detalle")
public class PedidoDetalle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer cantidad;
    @Column(name = "precio_unitario", precision = 10, scale = 2)
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private BigDecimal precioUnitario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "instrumento_id") // Nombre de columna en la BD
    private Instrumento instrumento;

    @ManyToOne
    @JoinColumn(name = "pedido_id")
    @JsonIgnore
    private Pedido pedido;

    public PedidoDetalle() {}

    public Long getId() {return id;}

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Instrumento getInstrumento() {
        return instrumento;
    }

    public void setInstrumento(Instrumento instrumento) {this.instrumento = instrumento;}

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public BigDecimal getPrecioUnitario() {return precioUnitario;}

    public void setPrecioUnitario(BigDecimal precioUnitario) {this.precioUnitario = precioUnitario;}
}
