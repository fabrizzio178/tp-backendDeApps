package com.tpi.ms_tarifas.dto.transporte;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TramoDTO {
    private Long id;
    private BigDecimal costoEstimado;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private PuntoDTO punto;
    private PuntoDTO puntoDestino;
}
