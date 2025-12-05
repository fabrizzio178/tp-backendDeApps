package com.tpi.ms_tarifas.dto.transporte;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RutaDTO {
    private Long id;
    private Integer idDeposito;
}
