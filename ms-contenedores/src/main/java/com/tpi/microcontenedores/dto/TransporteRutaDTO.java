package com.tpi.microcontenedores.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransporteRutaDTO {
    private Long id;
}
