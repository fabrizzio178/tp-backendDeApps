package com.tpi.microcontenedores.dto;

import java.time.LocalDateTime;

public class SolicitudResponseDTO {
    private Long id;
    private String estado;
    private Double costoEstimado;
    private Double tiempoEstimado;
    private Integer costoFinal;
    private Integer tiempoReal;
    private Integer idRuta;
    private Integer idCliente;
    private Integer idTarifa;
    private String observaciones;
    private LocalDateTime fechaCreacion;
    private ContenedorDTO contenedor;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Double getCostoEstimado() { return costoEstimado; }
    public void setCostoEstimado(Double costoEstimado) { this.costoEstimado = costoEstimado; }

    public Double getTiempoEstimado() { return tiempoEstimado; }
    public void setTiempoEstimado(Double tiempoEstimado) { this.tiempoEstimado = tiempoEstimado; }

    public Integer getIdRuta() { return idRuta; }
    public void setIdRuta(Integer idRuta) { this.idRuta = idRuta; }

    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }

    public Integer getIdTarifa() { return idTarifa; }
    public void setIdTarifa(Integer idTarifa) { this.idTarifa = idTarifa; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public ContenedorDTO getContenedor() { return contenedor; }
    public void setContenedor(ContenedorDTO contenedor) { this.contenedor = contenedor; }

    public Integer getCostoFinal() {
        return costoFinal;
    }

    public void setCostoFinal(Integer costoFinal) {
        this.costoFinal = costoFinal;
    }

    public Integer getTiempoReal() {
        return tiempoReal;
    }

    public void setTiempoReal(Integer tiempoReal) {
        this.tiempoReal = tiempoReal;
    }
}
