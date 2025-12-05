package com.tpi.microcontenedores.dto;

import java.math.BigDecimal;

public class ContenedorDTO {
    private Long id;
    private BigDecimal peso;
    private BigDecimal altura;
    private BigDecimal ancho;
    private BigDecimal largo;
    private BigDecimal volumen;
    private String estado;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getPeso() { return peso; }
    public void setPeso(BigDecimal peso) { this.peso = peso; }

    public BigDecimal getAltura() { return altura; }
    public void setAltura(BigDecimal altura) { this.altura = altura; }
    public BigDecimal getAncho() { return ancho; }
    public void setAncho(BigDecimal ancho) { this.ancho = ancho; }

    public BigDecimal getLargo() { return largo; }
    public void setLargo(BigDecimal largo) { this.largo = largo; }

    public BigDecimal getVolumen() { return volumen; }
    public void setVolumen(BigDecimal volumen) { this.volumen = volumen; }


    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
