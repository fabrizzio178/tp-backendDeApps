package com.tpi.ms_tarifas.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.tpi.ms_tarifas.dto.CostoRequest;
import com.tpi.ms_tarifas.dto.CostoResponse;
import com.tpi.ms_tarifas.dto.CostoRutaDetalle;
import com.tpi.ms_tarifas.dto.CostoRutaResponse;
import com.tpi.ms_tarifas.dto.UpdateTarifaRequest;
import com.tpi.ms_tarifas.dto.transporte.DepositoDTO;
import com.tpi.ms_tarifas.dto.transporte.PuntoDTO;
import com.tpi.ms_tarifas.dto.transporte.RutaDTO;
import com.tpi.ms_tarifas.dto.transporte.TramoDTO;
import com.tpi.ms_tarifas.entities.Tarifa;
import com.tpi.ms_tarifas.helpers.TransporteClient;
import com.tpi.ms_tarifas.repository.TarifaRepository;

@Service
public class TarifaService {
    private final TarifaRepository repo;
    private final JdbcTemplate jdbc;
    private final TransporteClient transporteClient;
    private final Logger log = LoggerFactory.getLogger(TarifaService.class);

    public TarifaService(TarifaRepository repo,
                         JdbcTemplate jdbc,
                         RestTemplate restTemplate,
                         TransporteClient transporteClient) {
        this.repo = repo;
        this.jdbc = jdbc;
        this.transporteClient = transporteClient;
    }

    @Transactional(readOnly = true)
    public List<Tarifa> findAll() {
        log.info("Obteniendo todas las tarifas");
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Tarifa> findById(Long id) {
        log.info("Obteniendo tarifa con ID: {}", id);
        return repo.findById(id);
    }

    @Transactional
    public ResponseEntity<Tarifa> registrarTarifa(Tarifa tarifa, Long idSolicitud) {
        if(tarifa.getId() != null) {
            log.warn("Intento de crear una tarifa con ID no nulo: {}", tarifa.getId());
            throw new IllegalArgumentException("El id de la tarifa debe ser nulo al crear una nueva tarifa");
        }
        log.info("Registrando nueva tarifa: {}", tarifa);
        Tarifa saved = repo.save(tarifa);
        asignarTarifaASolicitud(idSolicitud, saved.getId());
        return ResponseEntity.ok(saved);
    }

    @Transactional
    public ResponseEntity<Tarifa> update(UpdateTarifaRequest req) {
        log.info("Actualizando tarifa con ID: {}", req.getId());
        if (req.getId() == null) {
            log.warn("Intento de actualizar una tarifa sin ID");
            throw new IllegalArgumentException("El id de la tarifa es obligatorio");
        }
        log.info("Buscando tarifa existente con ID: {}", req.getId());
        Tarifa existing = repo.findById(req.getId()).orElse(null);
        if (existing == null) {
            log.warn("Tarifa con ID: {} no encontrada para actualizar", req.getId());
            return ResponseEntity.notFound().build();
        }

        if (req.getValorCostoKmVolumen() != null) existing.setValorCostoKmVolumen(req.getValorCostoKmVolumen());
        if (req.getValorLitro() != null) existing.setValorLitro(req.getValorLitro());
        if (req.getConsumoPromedio() != null) existing.setConsumoPromedio(req.getConsumoPromedio());
        if (req.getIdDeposito() != null) existing.setIdDeposito(req.getIdDeposito());
        if (req.getDominioCamion() != null) existing.setDominioCamion(req.getDominioCamion());
        if (req.getIdContenedor() != null) existing.setIdContenedor(req.getIdContenedor());
        if (req.getCargosGestion() != null) existing.setCargosGestion(req.getCargosGestion());
        if (req.getFechaVigencia() != null) existing.setFechaVigencia(req.getFechaVigencia());

        log.info("Guardando tarifa actualizada con ID: {}", req.getId());
        return ResponseEntity.ok(repo.save(existing));
    }

    public CostoResponse calcularCosto(CostoRequest req) {
        log.info("Calculando costo para tarifa ID: {} y distancia Km: {}", req.getIdTarifa(), req.getDistanciaKm());
        if (req.getIdTarifa() == null || req.getDistanciaKm() == null) {
            log.warn("idTarifa y distanciaKm son obligatorios");
            throw new IllegalArgumentException("idTarifa y distanciaKm son obligatorios");
        }
        log.info("Buscando tarifa con ID: {}", req.getIdTarifa());
        Tarifa tarifa = repo.findById(req.getIdTarifa()).orElse(null);
        if (tarifa == null){
            log.warn("Tarifa con ID: {} no encontrada", req.getIdTarifa());
            return null;
        }

        BigDecimal distanciaKm = defaultZero(req.getDistanciaKm());
        BigDecimal volumenM3 = req.getVolumenM3();
        if (volumenM3 == null && tarifa.getIdContenedor() != null) {
            log.info("Volumen no proporcionado, obteniendo volumen del contenedor con ID: {}", tarifa.getIdContenedor());
            volumenM3 = obtenerVolumenContenedor(tarifa.getIdContenedor());
        }
        volumenM3 = defaultZero(volumenM3);

        BigDecimal valorCostoKmVolumen = defaultZero(tarifa.getValorCostoKmVolumen());
        BigDecimal valorLitro = defaultZero(tarifa.getValorLitro());
        BigDecimal consumoPromedio = defaultZero(tarifa.getConsumoPromedio()); // litros/100km asumido
        BigDecimal cargosGestion = defaultZero(tarifa.getCargosGestion());

        BigDecimal costoPorKmVolumen = valorCostoKmVolumen
                .multiply(distanciaKm)
                .multiply(volumenM3)
                .setScale(2, RoundingMode.HALF_UP);
                log.info("Costo por Km y Volumen calculado: {}", costoPorKmVolumen);

        BigDecimal litrosConsumidos = distanciaKm
                .multiply(consumoPromedio)
                .divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
                log.info("Litros consumidos calculados: {}", litrosConsumidos);

        BigDecimal costoCombustible = litrosConsumidos
                .multiply(valorLitro)
                .setScale(2, RoundingMode.HALF_UP);
                log.info("Costo de combustible calculado: {}", costoCombustible);

        BigDecimal costoTotal = costoPorKmVolumen
                .add(costoCombustible)
                .add(cargosGestion)
                .setScale(2, RoundingMode.HALF_UP);
                log.info("Costo total calculado: {}", costoTotal);

        log.info("Costo calculado exitosamente para tarifa ID: {}", req.getIdTarifa());
        return new CostoResponse(
            tarifa.getId(),
            distanciaKm.setScale(2, RoundingMode.HALF_UP),
            volumenM3.setScale(3, RoundingMode.HALF_UP),
            costoPorKmVolumen,
            costoCombustible,
            cargosGestion.setScale(2, RoundingMode.HALF_UP),
            costoTotal,
            tarifa.getFechaVigencia()
        );
    }

    // PIDE ESTE COMUNICACION MICROSERVICIOS
    @Transactional(readOnly = true)
    public CostoRutaResponse calcularCostosRuta(Long idRuta, String token) {
        if (idRuta == null) {
            log.warn("Intento de calcular costos de ruta sin ID");
            throw new IllegalArgumentException("El id de la ruta es obligatorio");
        }

        RutaDTO ruta = obtenerRutaRemota(idRuta, token);
        if (ruta == null) {
            log.warn("Ruta con ID: {} no encontrada en el servicio de transporte", idRuta);
            throw new IllegalArgumentException("Ruta no encontrada");
        }

        List<TramoDTO> tramos = obtenerTramosPorRuta(idRuta, token);
        if (tramos.isEmpty()) {
            log.info("No se encontraron tramos para la ruta con ID: {}", idRuta);
            return new CostoRutaResponse(
                    idRuta,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    Collections.emptyList());
        }

        Map<Long, BigDecimal> cacheCostosDeposito = new HashMap<>();
        BigDecimal costoDepositoRuta = obtenerCostoDeposito(ruta.getIdDeposito(), cacheCostosDeposito, token);

        BigDecimal totalEstimado = BigDecimal.ZERO;
        BigDecimal totalEstadia = BigDecimal.ZERO;
        List<CostoRutaDetalle> detalles = new ArrayList<>();

        for (TramoDTO tramo : tramos) {
            log.info("Calculando costos para tramo ID: {}", tramo.getId());
            BigDecimal estimado = defaultZero(tramo.getCostoEstimado());
            totalEstimado = totalEstimado.add(estimado);

            long diasEstadia = calcularDiasEstadia(tramo.getFechaHoraInicio(), tramo.getFechaHoraFin());

            BigDecimal costoDiario = obtenerCostoDeposito(tramo.getPuntoDestino(), cacheCostosDeposito, token);
            if (costoDiario.compareTo(BigDecimal.ZERO) == 0) {
                costoDiario = obtenerCostoDeposito(tramo.getPunto(), cacheCostosDeposito, token);
            }
            if (costoDiario.compareTo(BigDecimal.ZERO) == 0) {
                costoDiario = costoDepositoRuta;
            }

            BigDecimal costoEstadiaReal = diasEstadia > 0
                    ? costoDiario.multiply(BigDecimal.valueOf(diasEstadia))
                    : BigDecimal.ZERO;

            totalEstadia = totalEstadia.add(costoEstadiaReal);

            detalles.add(new CostoRutaDetalle(
                    tramo.getId(),
                    estimado,
                    diasEstadia,
                    costoEstadiaReal));
        }
        log.info("Costos calculados exitosamente para ruta ID: {}", idRuta);
        return new CostoRutaResponse(
                idRuta,
                totalEstimado,
                totalEstadia,
                totalEstimado.add(totalEstadia),
                detalles);
    }

    private BigDecimal obtenerVolumenContenedor(Long idContenedor) {
        try {
            // volumen es columna generada en DB (altura*ancho*largo)
            BigDecimal vol = jdbc.queryForObject(
                "SELECT COALESCE(volumen, 0) FROM logistica.contenedor WHERE id_contenedor = ?",
                BigDecimal.class,
                idContenedor
            );
            log.info("Volumen del contenedor con ID {}: {}", idContenedor, vol);
            return vol == null ? BigDecimal.ZERO : vol;
        } catch (EmptyResultDataAccessException e) {
            log.warn("Contenedor con ID {} no encontrado al obtener volumen", idContenedor);
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal defaultZero(BigDecimal v) {
        log.info("Valor recibido para defaultZero: {}", v);
        return v == null ? BigDecimal.ZERO : v;
    }

    private void asignarTarifaASolicitud(Long idSolicitud, Long idTarifa) {
        if (idSolicitud == null || idTarifa == null) {
            return;
        }
        log.info("Asignando tarifa ID: {} a solicitud ID: {}", idTarifa, idSolicitud);
        int updated = jdbc.update(
            "UPDATE logistica.solicitud_transporte SET id_tarifa = ? WHERE id_solicitud = ?",
            idTarifa,
            idSolicitud
        );
        if (updated == 0) {
            log.warn("No se encontró solicitud con ID: {} para asignar tarifa ID: {}", idSolicitud, idTarifa);
        } else {
            log.info("Tarifa ID: {} asignada exitosamente a solicitud ID: {}", idTarifa, idSolicitud);
        }
    }


    private RutaDTO obtenerRutaRemota(Long idRuta, String token) {
        try {
            log.info("Obteniendo ruta remota con ID: {}", idRuta);
            return transporteClient.obtenerRuta(idRuta, token);
        } catch (RestClientException e) {
            log.error("Error al obtener ruta remota con ID {}: {}", idRuta, e.getMessage());
            return null;
        }
    }

    private List<TramoDTO> obtenerTramosPorRuta(Long idRuta, String token) {
        try {
            log.info("Obteniendo tramos para la ruta con ID: {}", idRuta);
            return transporteClient.obtenerTramos(idRuta, token);
        } catch (RestClientException e) {
            log.error("Error al obtener tramos para la ruta con ID {}: {}", idRuta, e.getMessage());
            return Collections.emptyList();
        }
    }

    private DepositoDTO obtenerDepositoRemoto(Long idDeposito, String token) {
        if (idDeposito == null) {
            log.info("ID de depósito es null, no se puede obtener depósito remoto");
            return null;
        }
        try {
            log.info("Obteniendo depósito remoto con ID: {}", idDeposito);
            return transporteClient.obtenerDeposito(idDeposito, token);
        } catch (RestClientException e) {
            log.error("Error al obtener depósito remoto con ID {}: {}", idDeposito, e.getMessage());
            return null;
        }
    }

    private BigDecimal obtenerCostoDeposito(PuntoDTO punto, Map<Long, BigDecimal> cache, String token) {
        if (punto == null || punto.getIdDeposito() == null) {
            log.info("Punto o ID de depósito es null, no se puede obtener costo de depósito");
            return BigDecimal.ZERO;
        }
        return obtenerCostoDepositoPorId(punto.getIdDeposito().longValue(), cache, token);
    }

    private BigDecimal obtenerCostoDeposito(Integer idDeposito, Map<Long, BigDecimal> cache, String token) {
        if (idDeposito == null) {
            log.info("ID de depósito es null, no se puede obtener costo de depósito");
            return BigDecimal.ZERO;
        }
        return obtenerCostoDepositoPorId(idDeposito.longValue(), cache, token);
    }

    private BigDecimal obtenerCostoDepositoPorId(Long idDeposito, Map<Long, BigDecimal> cache, String token) {
        if (idDeposito == null) {
            log.info("ID de depósito es null, no se puede obtener costo de depósito");
            return BigDecimal.ZERO;
        }
        if (cache.containsKey(idDeposito)) {
            log.info("Costo de depósito con ID {} obtenido de caché", idDeposito);
            return cache.get(idDeposito);
        }
        DepositoDTO deposito = obtenerDepositoRemoto(idDeposito, token);
        BigDecimal costo = (deposito != null && deposito.getCostoEstadia() != null)
                ? deposito.getCostoEstadia()
                : BigDecimal.ZERO;
        cache.put(idDeposito, costo);
        return costo;
    }

    private long calcularDiasEstadia(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null) {
            log.info("Fecha de inicio o fin es null, no se puede calcular días de estadía");
            return 0L;
        }
        Duration duracion = Duration.between(inicio, fin);
        long segundos = duracion.getSeconds();
        if (segundos <= 0) {
            log.info("Duración negativa o cero, no se puede calcular días de estadía");
            return 0L;
        }
        return (long) Math.ceil(segundos / (60d * 60d * 24d));
    }
}

