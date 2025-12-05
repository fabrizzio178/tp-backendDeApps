package com.tpi.ms_transporte.services.strategy;

import com.tpi.ms_transporte.dto.DistanciaDTO;
import com.tpi.ms_transporte.entities.Camion;
import com.tpi.ms_transporte.entities.Deposito;
import com.tpi.ms_transporte.entities.Punto;
import com.tpi.ms_transporte.entities.Tramo;
import com.tpi.ms_transporte.repository.DepositoRepository;
import com.tpi.ms_transporte.services.CamionService;
import com.tpi.ms_transporte.services.MapsService;
import com.tpi.ms_transporte.services.ParametrosService;
import com.tpi.ms_transporte.services.strategy.interfaces.IEstrategiaRutasTentativas;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EstrategiaRutasConDeposito implements IEstrategiaRutasTentativas {

    private final MapsService mapsService;
    private final ParametrosService parametrosService;
    private final DepositoRepository depositoRepo;
    private final CamionService camionService;
    private final Logger log = LoggerFactory.getLogger(EstrategiaRutasConDeposito.class);


    public EstrategiaRutasConDeposito(
            MapsService mapsService,
            ParametrosService parametrosService,
            DepositoRepository depositoRepo,
            CamionService camionService
    ) {
        this.mapsService = mapsService;
        this.parametrosService = parametrosService;
        this.depositoRepo = depositoRepo;
        this.camionService = camionService;

    }

    @Override
    public Map<String, Object> calcularRutasTentativas(ParametrosRutaTentativa params) throws Exception {
        log.info("Iniciando cálculo de rutas tentativas con parámetros: {}", params);
        double latOrigen = params.getLatOrigen();
        double lonOrigen = params.getLonOrigen();
        double latDestino = params.getLatDestino();
        double lonDestino = params.getLonDestino();

        Punto origen = crearPunto(latOrigen, lonOrigen);
        Punto destino = crearPunto(latDestino, lonDestino);

        Camion camion = camionesDisponiblesPromedio();

        // Ruta directa
        DistanciaDTO tramoDirecto = mapsService.calcularEntrePuntos(latOrigen, lonOrigen, latDestino, lonDestino);
        BigDecimal costoDirecto = calcularCostoTramo(construirTramo(origen, destino), camion, null);

        Map<String, Object> rutaDirecta = new HashMap<>();
        rutaDirecta.put("DistanciaTotalKm", tramoDirecto.getDistanciaKm());
        rutaDirecta.put("TiempoTotalHoras", tramoDirecto.getDuracionHoras());
        rutaDirecta.put("CostoTotalEstimado", costoDirecto);
        log.info("Ruta directa calculada: {}", rutaDirecta);

        double distanciaTotal = tramoDirecto.getDistanciaKm();
        log.info("Distancia total de la ruta directa: {} km", distanciaTotal);

        // Si distancia <= 200, no hay paradas
        if (distanciaTotal <= 200) {
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("RequiereParada", false);
            resultado.put("RutaDirecta", rutaDirecta);
            resultado.put("RutasConDepositos", List.of());
            log.info("No se requieren paradas en la ruta directa.");
            return resultado;
        }

        // Buscar depósitos candidatos
        List<Deposito> depositos = depositoRepo.findAll();
        log.info("Depósitos totales encontrados en repositorio: {}", depositos.size());
        List<DepositoCandidato> candidatos = filtrarDepositosSobreRuta(origen, destino, depositos);
        log.info("Depósitos candidatos encontrados: {}", candidatos.size());

        if (candidatos.isEmpty()) {
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("RequiereParada", true);
            resultado.put("RutaDirecta", rutaDirecta);
            resultado.put("Mensaje", "No se encontraron depósitos adecuados en la ruta.");
            resultado.put("RutasConDepositos", List.of());
            log.warn("No se encontraron depósitos adecuados en la ruta.");
            return resultado;
        }

        // Elegir 1 o 2 depósitos según reglas y balance de distancias
        List<Deposito> depositosElegidos = elegirDepositosEquilibrados(
            candidatos,
            distanciaTotal,
            tramoDirecto.getDistanciaKm()
        );
        List<Map<String, Object>> rutasConDepositos = new ArrayList<>();

        //----------------------------------------------------------------------
        // CASO 1: EXACTAMENTE 1 DEPÓSITO
        //----------------------------------------------------------------------
        if (depositosElegidos.size() == 1) {

            Deposito dep = depositosElegidos.get(0);

            double latDep = dep.getLatitud().doubleValue();
            double lonDep = dep.getLongitud().doubleValue();


            DistanciaDTO t1 = mapsService.calcularEntrePuntos(latOrigen, lonOrigen, latDep, lonDep);
            DistanciaDTO t2 = mapsService.calcularEntrePuntos(latDep, lonDep, latDestino, lonDestino);

            BigDecimal c1 = calcularCostoTramo(construirTramo(origen, crearPunto(latDep, lonDep)), camion, dep);
            BigDecimal c2 = calcularCostoTramo(construirTramo(crearPunto(latDep, lonDep), destino), camion, dep);

            Map<String, Object> ruta1 = new HashMap<>();
            ruta1.put("Depositos", List.of(dep.getNombre()));
            ruta1.put("DistanciaTotalKm", t1.getDistanciaKm() + t2.getDistanciaKm());
            ruta1.put("TiempoTotalHoras", t1.getDuracionHoras() + t2.getDuracionHoras());
            ruta1.put("CostoTotalEstimado", round(c1.doubleValue() + c2.doubleValue()));
            log.info("Ruta con un depósito calculada: {}", ruta1);

            ruta1.put("Detalles", List.of(
                    Map.of(
                            "Segmento", "Origen - " + dep.getNombre(),
                            "DistanciaKm", t1.getDistanciaKm(),
                            "TiempoHoras", t1.getDuracionHoras(),
                            "CostoEstimado", round(c1.doubleValue())
                    ),
                    Map.of(
                            "Segmento", dep.getNombre() + " - Destino",
                            "DistanciaKm", t2.getDistanciaKm(),
                            "TiempoHoras", t2.getDuracionHoras(),
                            "CostoEstimado", round(c2.doubleValue())
                    )
            ));

            rutasConDepositos.add(ruta1);
        }

        //----------------------------------------------------------------------
        // CASO 2: EXACTAMENTE 2 DEPÓSITOS (A y B)
        //----------------------------------------------------------------------
        if (depositosElegidos.size() == 2) {

            Deposito depA = depositosElegidos.get(0);
            Deposito depB = depositosElegidos.get(1);

            Punto pA = crearPunto(depA.getLatitud().doubleValue(), depA.getLongitud().doubleValue());
            Punto pB = crearPunto(depB.getLatitud().doubleValue(), depB.getLongitud().doubleValue());

            DistanciaDTO t1 = mapsService.calcularEntrePuntos(latOrigen, lonOrigen, pA.getLatitud(), pA.getLongitud());
            DistanciaDTO t2 = mapsService.calcularEntrePuntos(pA.getLatitud(), pA.getLongitud(), pB.getLatitud(), pB.getLongitud());
            DistanciaDTO t3 = mapsService.calcularEntrePuntos(pB.getLatitud(), pB.getLongitud(), latDestino, lonDestino);

            BigDecimal c1 = calcularCostoTramo(construirTramo(origen, pA), camion, depA);
            BigDecimal c2 = calcularCostoTramo(construirTramo(pA, pB), camion, depB);
            BigDecimal c3 = calcularCostoTramo(construirTramo(pB, destino), camion, depB);

            Map<String, Object> ruta2 = new HashMap<>();
            ruta2.put("Depositos", List.of(depA.getNombre(), depB.getNombre()));
            ruta2.put("DistanciaTotalKm", t1.getDistanciaKm() + t2.getDistanciaKm() + t3.getDistanciaKm());
            ruta2.put("TiempoTotalHoras", t1.getDuracionHoras() + t2.getDuracionHoras() + t3.getDuracionHoras());
            ruta2.put("CostoTotalEstimado", round(c1.doubleValue() + c2.doubleValue() + c3.doubleValue()));

            ruta2.put("Detalles", List.of(
                    Map.of(
                            "Segmento", "Origen - " + depA.getNombre(),
                            "DistanciaKm", t1.getDistanciaKm(),
                            "TiempoHoras", t1.getDuracionHoras(),
                            "CostoEstimado", round(c1.doubleValue())
                    ),
                    Map.of(
                            "Segmento", depA.getNombre() + " - " + depB.getNombre(),
                            "DistanciaKm", t2.getDistanciaKm(),
                            "TiempoHoras", t2.getDuracionHoras(),
                            "CostoEstimado", round(c2.doubleValue())
                    ),
                    Map.of(
                            "Segmento", depB.getNombre() + " - Destino",
                            "DistanciaKm", t3.getDistanciaKm(),
                            "TiempoHoras", t3.getDuracionHoras(),
                            "CostoEstimado", round(c3.doubleValue())
                    )
            ));

            rutasConDepositos.add(ruta2);
        }

        // Resultado final
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("RequiereParada", true);
        resultado.put("RutaDirecta", rutaDirecta);
        resultado.put("RutasConDepositos", rutasConDepositos);
        log.info("Resultado de rutas con depósitos calculadas: {}", resultado);

        return resultado;
    }



    // ==================================================================================
    // Métodos auxiliares
    // ==================================================================================

    private Punto crearPunto(double lat, double lon) {
        Punto p = new Punto();
        p.setLatitud(lat);
        p.setLongitud(lon);
        return p;
    }

    private Tramo construirTramo(Punto origen, Punto destino) {
        Tramo t = new Tramo();
        t.setPunto(origen);
        t.setPuntoDestino(destino);
        return t;
    }

    private boolean depositoEstaEnRuta(Punto origen, Punto destino, Deposito deposito) {
        double distancia = distanciaAPuntoALinea(
                origen.getLatitud(), origen.getLongitud(),
                destino.getLatitud(), destino.getLongitud(),
                deposito.getLatitud().doubleValue(), deposito.getLongitud().doubleValue()
        );
        return distancia <= 70; // dentro de 70 km del trayecto
    }

    private double distanciaAPuntoALinea(double lat1, double lon1, double lat2, double lon2, double latP, double lonP) {
        double A = latP - lat1;
        double B = lonP - lon1;
        double C = lat2 - lat1;
        double D = lon2 - lon1;

        double dot = A * C + B * D;
        double lenSq = C * C + D * D;

        if (lenSq == 0) return Math.sqrt(A * A + B * B);
        double t = Math.max(0, Math.min(1, dot / lenSq));

        double projX = lat1 + t * C;
        double projY = lon1 + t * D;

        double distX = projX - latP;
        double distY = projY - lonP;

        return Math.sqrt(distX * distX + distY * distY) * 111; // conversión a km aprox.
    }

    private List<DepositoCandidato> filtrarDepositosSobreRuta(Punto origen, Punto destino, List<Deposito> depositos) {
        List<DepositoCandidato> lista = new ArrayList<>();
        double directDistance;
        try {
            directDistance = mapsService.calcularEntrePuntos(
                    origen.getLatitud(), origen.getLongitud(),
                    destino.getLatitud(), destino.getLongitud()
            ).getDistanciaKm();
        } catch (Exception e) {
            // si falla el cálculo directo, lo dejamos en 0 para no romper todo
            directDistance = 0;
        }

        for (Deposito dep : depositos) {
            try {
                if (dep.getLatitud() == null || dep.getLongitud() == null) continue;

                // chequeo ortogonal: que esté dentro de la franja del recorrido (tu criterio: <=70km)
                if (!depositoEstaEnRuta(origen, destino, dep)) continue;

                double latDep = dep.getLatitud().doubleValue();
                double lonDep = dep.getLongitud().doubleValue();

                DistanciaDTO d1 = mapsService.calcularEntrePuntos(
                        origen.getLatitud(), origen.getLongitud(),
                        latDep, lonDep);
                DistanciaDTO d2 = mapsService.calcularEntrePuntos(
                        latDep, lonDep,
                        destino.getLatitud(), destino.getLongitud());

                // evitar depósitos que queden prácticamente en el origen o destino
                double minThresholdKm = 2.0; // configurable: no menos de 2 km del origen/destino
                if (d1.getDistanciaKm() < minThresholdKm || d2.getDistanciaKm() < minThresholdKm) continue;

                // penalización = (origen->dep + dep->dest) - directo
                double penalizacion = (d1.getDistanciaKm() + d2.getDistanciaKm()) - directDistance;

                lista.add(new DepositoCandidato(dep, d1.getDistanciaKm(), d2.getDistanciaKm(), penalizacion));
            } catch (Exception ex) {
                // ignorar depósitos que den error en Maps
            }
        }

        return lista.stream()
                .sorted(Comparator.comparingDouble(DepositoCandidato::penalizacionExtra))
                .collect(Collectors.toList());
    }

    private List<Deposito> elegirDepositosEquilibrados(List<DepositoCandidato> candidatos,
                                                       double distanciaTotal,
                                                       double distanciaDirecta) {

        Deposito mejorIndividual = null;
        if (distanciaTotal > 200 && !candidatos.isEmpty()) {
            mejorIndividual = seleccionarMejorDeposito(candidatos);
        }

        if (distanciaTotal > 400 && candidatos.size() >= 2) {
            List<Deposito> pareja = seleccionarMejorPareja(
                    candidatos,
                    distanciaDirecta
            );
            if (!pareja.isEmpty()) {
                return pareja;
            }
        }

        if (mejorIndividual != null) {
            return List.of(mejorIndividual);
        }

        return List.of();
    }

    private Deposito seleccionarMejorDeposito(List<DepositoCandidato> candidatos) {
        return candidatos.stream()
                .min(Comparator
                        .comparingDouble(DepositoCandidato::balanceScore)
                        .thenComparingDouble(DepositoCandidato::penalizacionExtra))
                .map(DepositoCandidato::deposito)
                .orElse(null);
    }

    private List<Deposito> seleccionarMejorPareja(List<DepositoCandidato> candidatos,
                                                  double distanciaDirecta) {

        double mejorBalance = Double.MAX_VALUE;
        double mejorPenalizacion = Double.MAX_VALUE;
        List<Deposito> mejorPar = List.of();

        for (int i = 0; i < candidatos.size(); i++) {
            for (int j = i + 1; j < candidatos.size(); j++) {
                DepositoCandidato a = candidatos.get(i);
                DepositoCandidato b = candidatos.get(j);

                DepositoCandidato primero = a.distanciaDesdeOrigen <= b.distanciaDesdeOrigen ? a : b;
                DepositoCandidato segundo = primero == a ? b : a;

                try {
                    DistanciaDTO intermedio = mapsService.calcularEntrePuntos(
                            primero.deposito.getLatitud().doubleValue(),
                            primero.deposito.getLongitud().doubleValue(),
                            segundo.deposito.getLatitud().doubleValue(),
                            segundo.deposito.getLongitud().doubleValue()
                    );

                    double d1 = primero.distanciaDesdeOrigen;
                    double d2 = intermedio.getDistanciaKm();
                    double d3 = segundo.distanciaHastaDestino;

                    double max = Math.max(d1, Math.max(d2, d3));
                    double min = Math.min(d1, Math.min(d2, d3));
                    double balance = max - min;

                    double penalizacion = (d1 + d2 + d3) - distanciaDirecta;

                    if (balance < mejorBalance || (balance == mejorBalance && penalizacion < mejorPenalizacion)) {
                        mejorBalance = balance;
                        mejorPenalizacion = penalizacion;
                        mejorPar = List.of(primero.deposito, segundo.deposito);
                    }

                } catch (Exception ignored) {
                    // si maps falla para esta pareja, seguimos con la siguiente
                }
            }
        }

        return mejorPar;
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
                log.info("Costo distancia calculado: {}", costoDistancia);

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

        log.info("Costo distancia: {}, Costo camión: {}, Costo combustible: {}", costoDistancia, costoCamion, costoCombustible);
        return costoDistancia.add(costoCamion).add(costoCombustible);
    }

    private double round(Double value){
        return Math.round(value * 1000.0) / 1000.0;
    }

    private static final class DepositoCandidato {
        private final Deposito deposito;
        private final double distanciaDesdeOrigen;
        private final double distanciaHastaDestino;
        private final double penalizacionExtra;

        private DepositoCandidato(Deposito deposito, double distanciaDesdeOrigen, double distanciaHastaDestino, double penalizacionExtra) {
            this.deposito = deposito;
            this.distanciaDesdeOrigen = distanciaDesdeOrigen;
            this.distanciaHastaDestino = distanciaHastaDestino;
            this.penalizacionExtra = penalizacionExtra;
        }

        private double balanceScore() {
            return Math.abs(distanciaDesdeOrigen - distanciaHastaDestino);
        }

        private Deposito deposito() {
            return deposito;
        }

        private double penalizacionExtra() {
            return penalizacionExtra;
        }
    }
}
