package com.tpi.ms_tarifas.dto.transporte;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DepositoDTO {
    private Long id;
    private BigDecimal costoEstadia;
}
