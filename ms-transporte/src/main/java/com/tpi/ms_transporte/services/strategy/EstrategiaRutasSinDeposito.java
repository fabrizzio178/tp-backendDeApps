package com.tpi.ms_transporte.services.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tpi.ms_transporte.dto.DistanciaDTO;
import com.tpi.ms_transporte.entities.Camion;
import com.tpi.ms_transporte.entities.Deposito;
import com.tpi.ms_transporte.entities.Punto;
import com.tpi.ms_transporte.entities.Tramo;
import com.tpi.ms_transporte.services.CamionService;
import com.tpi.ms_transporte.services.MapsService;
import com.tpi.ms_transporte.services.ParametrosService;
import com.tpi.ms_transporte.services.strategy.interfaces.IEstrategiaRutasTentativas;

public class EstrategiaRutasSinDeposito implements IEstrategiaRutasTentativas{

    private final MapsService mapsService;
    private final CamionService camionService;
    private final ParametrosService parametrosService;
    private final Logger log = LoggerFactory.getLogger(EstrategiaRutasSinDeposito.class);

    public EstrategiaRutasSinDeposito(MapsService mapsService, CamionService camionService, ParametrosService parametrosService){
        this.mapsService = mapsService;
        this.camionService = camionService;
        this.parametrosService = parametrosService;

    }


    @Override
    public Map<String, Object> calcularRutasTentativas(ParametrosRutaTentativa params) throws Exception {
        double latOrigen = params.getLatOrigen();
        double lonOrigen = params.getLonOrigen();
        double latDestino = params.getLatDestino();
        double lonDestino = params.getLonDestino();
        Punto origen = new Punto();
        origen.setLatitud(latOrigen);
        origen.setLongitud(lonOrigen);
        log.info("Punto origen establecido en latitud: {}, longitud: {}", latOrigen, lonOrigen);


        Punto destino = new Punto();
        destino.setLatitud(latDestino);
        destino.setLongitud(lonDestino);
        log.info("Punto destino establecido en latitud: {}, longitud: {}", latDestino, lonDestino);


        Tramo tramo = new Tramo();
        tramo.setPunto(origen);
        tramo.setPuntoDestino(destino);
        tramo.setTipo("ORIGEN-DESTINO");
        log.info("Tramo establecido desde origen a destino con tipo: {}", "ORIGEN-DESTINO");

        Camion camion = camionesDisponiblesPromedio();
        Deposito deposito = null; // No hay deposito en esta estrategia

        DistanciaDTO calculo = mapsService.calcularEntrePuntos(latOrigen, lonOrigen, latDestino, lonDestino);
        BigDecimal costoEstimadoTramo = calcularCostoTramo(tramo, camion, deposito);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("Tipo de Ruta", "Sin Deposito");
        resultado.put("DistanciaTotalKm", calculo.getDistanciaKm());
        resultado.put("TiempoTotalHoras", calculo.getDuracionHoras());
        resultado.put("CostoTotalEstimado", costoEstimadoTramo);
        log.info("Resultado de la ruta tentativa calculada: {}", resultado);

        return resultado;
             
    }

    private Camion camionesDisponiblesPromedio() {
        List<Camion> disponibles = camionService.findAll();
                if (disponibles.isEmpty()) {
                        Camion mock = new Camion();
                        mock.setCostoBase(parametrosService.getCostoKmBase());
                        mock.setConsumoCombustible(parametrosService.getConsumoPromedio());
                        return mock;
                }

                BigDecimal totalCostoBase = BigDecimal.ZERO;
                BigDecimal totalConsumo = BigDecimal.ZERO;
                int countCosto = 0;
                int countConsumo = 0;

                for (Camion c : disponibles) {
                        if (c.getCostoBase() != null) {
                                totalCostoBase = totalCostoBase.add(c.getCostoBase());
                                countCosto++;
                        }
                        if (c.getConsumoCombustible() != null) {
                                totalConsumo = totalConsumo.add(c.getConsumoCombustible());
                                countConsumo++;
                        }
                }

                BigDecimal costoBaseProm = countCosto > 0
                                ? totalCostoBase.divide(BigDecimal.valueOf(countCosto), RoundingMode.HALF_UP)
                                : parametrosService.getCostoKmBase();

                BigDecimal consumoProm = countConsumo > 0
                                ? totalConsumo.divide(BigDecimal.valueOf(countConsumo), RoundingMode.HALF_UP)
                                : parametrosService.getConsumoPromedio();

                Camion mock = new Camion();
                mock.setCostoBase(costoBaseProm);
                mock.setConsumoCombustible(consumoProm);
                return mock;
    }
    @Override
    public BigDecimal calcularCostoTramo(Tramo tramo, Camion camion, Deposito deposito) throws Exception {
        DistanciaDTO dto = mapsService.calcularEntrePuntos(
                tramo.getPunto().getLatitud(),
                tramo.getPunto().getLongitud(),
                tramo.getPuntoDestino().getLatitud(),
                tramo.getPuntoDestino().getLongitud()
        );
        log.info("Distancia calculada entre puntos: {} km, Duración estimada: {} horas", dto.getDistanciaKm(), dto.getDuracionHoras());

        double distanciaKm = dto.getDistanciaKm();
        log.info("Distancia en km para cálculo de costo: {}", distanciaKm);

        BigDecimal costoDistancia = parametrosService.getCostoKmBase()
                .multiply(BigDecimal.valueOf(distanciaKm));
        log.info("Costo por distancia (CostoKmBase * DistanciaKm): {}", costoDistancia);

        BigDecimal costoBaseCamion = (camion != null && camion.getCostoBase() != null)
                ? camion.getCostoBase()
                : parametrosService.getCostoKmBase();
        log.info("Costo base del camión utilizado para cálculo: {}", costoBaseCamion);

        BigDecimal consumoCamion = (camion != null && camion.getConsumoCombustible() != null)
                ? camion.getConsumoCombustible()
                : parametrosService.getConsumoPromedio();
        log.info("Consumo de combustible del camión utilizado para cálculo: {}", consumoCamion);

        BigDecimal costoCamion = costoBaseCamion
                .multiply(BigDecimal.valueOf(distanciaKm));
        log.info("Costo del camión (CostoBaseCamion * DistanciaKm): {}", costoCamion);

        BigDecimal costoCombustible = consumoCamion
                .multiply(BigDecimal.valueOf(distanciaKm))
                .multiply(parametrosService.getLitroCombustible());
        log.info("Costo del combustible (ConsumoCamion * DistanciaKm * LitroCombustible): {}", costoCombustible);

        return costoDistancia.add(costoCamion).add(costoCombustible);
    }
    
}
