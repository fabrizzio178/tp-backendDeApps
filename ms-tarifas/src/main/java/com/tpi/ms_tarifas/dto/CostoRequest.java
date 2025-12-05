package com.tpi.ms_tarifas.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CostoRequest {
    private Long idTarifa;
    private BigDecimal distanciaKm;
    // Si no se envía, se intenta obtener del contenedor asociado a la tarifa
    private BigDecimal volumenM3;
}

