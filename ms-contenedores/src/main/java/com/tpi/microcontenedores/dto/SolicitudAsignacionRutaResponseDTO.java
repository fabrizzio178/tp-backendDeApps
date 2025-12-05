package com.tpi.microcontenedores.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SolicitudAsignacionRutaResponseDTO {

    private SolicitudResponseDTO solicitud;
    private Map<String, Object> ruta;
    private List<TransporteTramoDTO> tramos;
    private String mensaje;
}
