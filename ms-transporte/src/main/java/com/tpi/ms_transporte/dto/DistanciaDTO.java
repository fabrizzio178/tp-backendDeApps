package com.tpi.ms_transporte.dto;

import lombok.Data;

@Data
public class DistanciaDTO {
    private String puntoOrigen;
    private String puntoDestino;
    private double distanciaKm;
    private String duracionTexto;
    private double duracionHoras;
}
