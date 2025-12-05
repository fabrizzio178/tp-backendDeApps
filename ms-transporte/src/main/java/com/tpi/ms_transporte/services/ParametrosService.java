package com.tpi.ms_transporte.services;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

@Service
public class ParametrosService {
    private static final BigDecimal COSTO_KM_BASE = new BigDecimal("120"); // Costo fijo por km
    private static final BigDecimal LITRO_COMBUSTIBLE = new BigDecimal("300"); // Costo fijo por kg
    private static final BigDecimal CONSUMO_PROMEDIO = new BigDecimal("0.38"); // LITROS/KM PROMEDIO

    public BigDecimal getCostoKmBase() {
        return COSTO_KM_BASE;
    }

    public BigDecimal getLitroCombustible() {
        return LITRO_COMBUSTIBLE;
    }

    public BigDecimal getConsumoPromedio() {
        return CONSUMO_PROMEDIO;
    }
}
