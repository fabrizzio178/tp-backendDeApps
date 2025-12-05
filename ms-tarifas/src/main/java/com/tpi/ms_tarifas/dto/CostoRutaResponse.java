package com.tpi.ms_tarifas.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CostoRutaResponse {
    private Long idRuta;
    private BigDecimal costoEstimadoTotal;
    private BigDecimal costoEstadiaReal;
    private BigDecimal costoRealTotal;
    private List<CostoRutaDetalle> detalles;
}
