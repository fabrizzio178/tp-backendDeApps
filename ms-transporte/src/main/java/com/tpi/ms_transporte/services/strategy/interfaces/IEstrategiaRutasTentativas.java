package com.tpi.ms_transporte.services.strategy.interfaces;

import java.math.BigDecimal;
import java.util.Map;

import com.tpi.ms_transporte.entities.Camion;
import com.tpi.ms_transporte.entities.Deposito;
import com.tpi.ms_transporte.entities.Tramo;
import com.tpi.ms_transporte.services.strategy.ParametrosRutaTentativa;

public interface IEstrategiaRutasTentativas {
    Map<String, Object> calcularRutasTentativas(ParametrosRutaTentativa params) throws Exception;
    BigDecimal calcularCostoTramo(Tramo tramo, Camion camion, Deposito deposito) throws Exception;
}
