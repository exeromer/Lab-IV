package com.utn.frm.instrumentos.dto;

import java.math.BigDecimal;

public class InstrumentoDTO {
    private Long id;
    private String instrumento;
    private BigDecimal precio;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getInstrumento() { return instrumento; }
    public void setInstrumento(String instrumento) { this.instrumento = instrumento; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
}