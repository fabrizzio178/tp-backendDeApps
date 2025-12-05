package com.tpi.microcontenedores.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoordenadasDTO {
    private Long idRuta; // opcional, puede ser nulo si la ruta no existe 
    private double latOrigen;
    private double lonOrigen;
    private double latDestino;
    private double lonDestino;
}
