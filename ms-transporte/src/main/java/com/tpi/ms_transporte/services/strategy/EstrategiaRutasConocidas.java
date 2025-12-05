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
import com.tpi.ms_transporte.entities.Tramo;
import com.tpi.ms_transporte.repository.TramoRepository;
import com.tpi.ms_transporte.services.CamionService;
import com.tpi.ms_transporte.services.DepositoService;
import com.tpi.ms_transporte.services.MapsService;
import com.tpi.ms_transporte.services.ParametrosService;
import com.tpi.ms_transporte.services.strategy.interfaces.IEstrategiaRutasTentativas;


public class EstrategiaRutasConocidas implements IEstrategiaRutasTentativas {
    private final Long idRuta;
    private final TramoRepository tramoRepository;
    private final MapsService mapsService;
    private final CamionService camionService;
    private final DepositoService depositoService;
    private final ParametrosService parametrosService;
    private final Logger log = LoggerFactory.getLogger(EstrategiaRutasConocidas.class);

    public EstrategiaRutasConocidas(Long idRuta, TramoRepository tramoRepository, MapsService mapsService, CamionService camionService, DepositoService depositoService, ParametrosService parametrosService){
        this.idRuta = idRuta;
        this.tramoRepository = tramoRepository;
        this.mapsService = mapsService;
        this.camionService = camionService;
        this.depositoService = depositoService;
        this.parametrosService = parametrosService;
    }

    @Override
    public Map<String, Object> calcularRutasTentativas(ParametrosRutaTentativa params) throws Exception {
        List<Tramo> tramos = tramoRepository.findByRutaId(idRuta);
        if (tramos.isEmpty()) {
            log.error("No se encontraron tramos para la ruta con ID: {}", idRuta);
            throw new Exception("No se encontraron tramos para la ruta con ID: " + idRuta);
        }

        Long idRuta = params.getIdRuta();

        List<Map<String, Object>> detallesTramos = tramos.stream().map(t -> {
            try {
                DistanciaDTO calc = mapsService.calcularEntrePuntos(
                        t.getPunto().getLatitud(),
                        t.getPunto().getLongitud(),
                        t.getPuntoDestino().getLatitud(),
                        t.getPuntoDestino().getLongitud());
                        log.info("Distancia calculada entre puntos para tramo ID {}: {} km, Duración estimada: {} horas", t.getId(), calc.getDistanciaKm(), calc.getDuracionHoras());

                Camion camion = t.getDominioCamion() != null
                        ? camionService.findByDominio(t.getDominioCamion())
                        : null;
                if (camion == null) {
                    camion = camionesDisponiblesPromedio();
                }

                Deposito deposito = null;
                if (t.getRuta() != null && t.getRuta().getIdDeposito() != null) {
                    deposito = depositoService.findById(t.getRuta().getIdDeposito().longValue());
                }

                BigDecimal costoTramo = calcularCostoTramo(t, camion, deposito);

                Map<String, Object> detalleTramo = new HashMap<>();
                detalleTramo.put("idTramo", t.getId());
                detalleTramo.put("distanciaKm", calc.getDistanciaKm());
                detalleTramo.put("duracionHoras", calc.getDuracionHoras());
                detalleTramo.put("costoEstimado", costoTramo);
                log.info("Detalle del tramo calculado: {}", detalleTramo);
                return detalleTramo;
            } catch (Exception e) {
                log.error("Error al calcular el tramo con ID: {}", t.getId(), e);
                throw new RuntimeException("Error al calcular el tramo con ID: " + t.getId(), e);
            }
        }).toList();

        double distanciaTotal = detallesTramos.stream()
                .mapToDouble(d -> ((Number) d.get("distanciaKm")).doubleValue())
                .sum();
        double tiempoTotal = detallesTramos.stream()
                .mapToDouble(d -> ((Number) d.get("duracionHoras")).doubleValue())
                .sum();
        BigDecimal costoTotal = detallesTramos.stream()
                .map(d -> (BigDecimal) d.get("costoEstimado"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("idRuta", idRuta);
        resultado.put("DistanciaTotalKm", distanciaTotal);
        resultado.put("TiempoTotalHoras", tiempoTotal);
        resultado.put("CostoTotalEstimado", costoTotal);
        resultado.put("detalles", detallesTramos);
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

        double distanciaKm = dto.getDistanciaKm();

        BigDecimal costoDistancia = parametrosService.getCostoKmBase()
                .multiply(BigDecimal.valueOf(distanciaKm));

        BigDecimal costoBaseCamion = (camion != null && camion.getCostoBase() != null)
                ? camion.getCostoBase()
                : parametrosService.getCostoKmBase();

        BigDecimal consumoCamion = (camion != null && camion.getConsumoCombustible() != null)
                ? camion.getConsumoCombustible()
                : parametrosService.getConsumoPromedio();

        BigDecimal costoCamion = costoBaseCamion
                .multiply(BigDecimal.valueOf(distanciaKm));

        BigDecimal costoCombustible = consumoCamion
                .multiply(BigDecimal.valueOf(distanciaKm))
                .multiply(parametrosService.getLitroCombustible());


        return costoDistancia.add(costoCamion).add(costoCombustible);
    }
    
}
