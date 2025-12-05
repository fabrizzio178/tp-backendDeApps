package com.tpi.ms_tarifas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CostoResponse {
    private Long idTarifa;
    private BigDecimal distanciaKm;
    private BigDecimal volumenM3;
    private BigDecimal costoPorKmVolumen;
    private BigDecimal costoCombustible;
    private BigDecimal cargosGestion;
    private BigDecimal costoTotal;
    private LocalDate fechaVigencia;
}

