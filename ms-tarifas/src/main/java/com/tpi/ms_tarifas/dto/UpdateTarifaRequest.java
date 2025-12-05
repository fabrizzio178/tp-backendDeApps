package com.tpi.ms_tarifas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTarifaRequest {
    private Long id;
    private BigDecimal valorCostoKmVolumen;
    private BigDecimal valorLitro;
    private BigDecimal consumoPromedio;
    private Long idDeposito;
    private String dominioCamion;
    private Long idContenedor;
    private BigDecimal cargosGestion;
    private LocalDate fechaVigencia;
}

