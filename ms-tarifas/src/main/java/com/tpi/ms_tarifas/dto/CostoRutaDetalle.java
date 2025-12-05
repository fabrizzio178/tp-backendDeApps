package com.tpi.ms_tarifas.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CostoRutaDetalle {
    private Long idTramo;
    private BigDecimal costoEstimado;
    private long diasEstadia;
    private BigDecimal costoRealEstadia;
}
