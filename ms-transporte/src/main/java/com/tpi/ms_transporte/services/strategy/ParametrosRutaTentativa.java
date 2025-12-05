package com.tpi.ms_transporte.services.strategy;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ParametrosRutaTentativa {
    private Long idRuta;
    private double latOrigen;
    private double lonOrigen;
    private double latDestino;
    private double lonDestino;
}
