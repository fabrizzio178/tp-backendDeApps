package com.tpi.ms_transporte.dto;

import java.math.BigDecimal;

public class ContenedorDTO {
    private Long id;
    private BigDecimal peso;
    private BigDecimal volumen;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BigDecimal getPeso() { return peso; }
    public void setPeso(BigDecimal peso) { this.peso = peso; }
    public BigDecimal getVolumen() { return volumen; }
    public void setVolumen(BigDecimal volumen) { this.volumen = volumen; }
}

