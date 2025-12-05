package com.tpi.microcontenedores.dto;

import com.tpi.microcontenedores.entities.Solicitud;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudRequestDTO {
    private Solicitud solicitud;
    private CoordenadasDTO coordenadas;
    private ClienteDTO cliente;
}
