package com.tpi.ms_tarifas.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tpi.ms_tarifas.dto.CostoRequest;
import com.tpi.ms_tarifas.dto.CostoResponse;
import com.tpi.ms_tarifas.dto.UpdateTarifaRequest;
import com.tpi.ms_tarifas.entities.Tarifa;
import com.tpi.ms_tarifas.services.TarifaService;

@RestController
@RequestMapping("/api/tarifas")
public class TarifaController {
    private final TarifaService service;
    private final Logger log = LoggerFactory.getLogger(TarifaController.class);

    public TarifaController(TarifaService service) {
        this.service = service;
    }

    // GET /api/tarifas
    @GetMapping
    public ResponseEntity<?> obtenerTarifas(@RequestParam(name = "id", required = false) Long id) {
        try {
            log.info("Obteniendo tarifas, filtro ID: {}", id);
            if (id != null) {
                Optional<Tarifa> t = service.findById(id);
                log.info("Tarifa encontrada: {}", t.isPresent());
                return t.<ResponseEntity<?>>map(ResponseEntity::ok)
                        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tarifa no encontrada"));
            }
            log.info("Obteniendo todas las tarifas");
            List<Tarifa> list = service.findAll();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener tarifas");
        }
    }

    // Requerimiento 8
    @GetMapping("/ruta/{id}/costos")
    public ResponseEntity<?> obtenerCostosPorRuta(@PathVariable Long id, @RequestHeader(value="Authorization", required=false) String token) {
        try {
            log.info("Obteniendo costos para ruta ID: {}", id);
            return ResponseEntity.ok(service.calcularCostosRuta(id, token));
        } catch (IllegalArgumentException ex) {
            log.warn("Error al calcular costos para ruta ID {}: {}", id, ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception e) {
            log.error("Error al calcular costos para ruta ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al calcular costos de la ruta");
        }
    }

    // PUT /api/tarifas
    @PutMapping("/{id}")
    public ResponseEntity<Tarifa> actualizarTarifa(@PathVariable Long id, @RequestBody UpdateTarifaRequest req) {
        try {
            req.setId(id);
            log.info("Actualizando tarifa con ID: {}", id);
            ResponseEntity<Tarifa> response = service.update(req);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("Error al actualizar tarifa con ID {}: Código de estado {}", id, response.getStatusCode());
                return ResponseEntity.status(response.getStatusCode()).body(null);
            }
            return ResponseEntity.ok(response.getBody());
        } catch (IllegalArgumentException ex) {
            log.warn("Error al actualizar tarifa con ID {}: {}", id, ex.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            log.error("Error al actualizar tarifa con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping
    public ResponseEntity<Tarifa> registrarTarifa(@RequestBody Tarifa tarifa,
                                                  @RequestParam(name = "idSolicitud", required = false) Long idSolicitud) {
        try {
            Tarifa saved = service.registrarTarifa(tarifa, idSolicitud).getBody();
            log.info("Registrando nueva tarifa: {}", saved);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(saved);
        } catch (IllegalArgumentException ex) {
            log.warn("Error al registrar tarifa: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            log.error("Error al registrar tarifa: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(null);
        }
    }


    // POST /api/tarifas/costo
    @PostMapping("/costo")
    public ResponseEntity<?> calcularYActualizarCostos(@RequestBody CostoRequest req) {
        try {
            log.info("Calculando costos para tarifa ID: {}", req.getIdTarifa());
            CostoResponse resp = service.calcularCosto(req);
            if (resp == null) {
                log.warn("Tarifa no encontrada para ID: {}", req.getIdTarifa());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tarifa no encontrada");
            }
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException ex) {
            log.warn("Error al calcular costos para tarifa ID {}: {}", req.getIdTarifa(), ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception e) {
            log.error("Error al calcular costos para tarifa ID {}: {}", req.getIdTarifa(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al calcular costos");
        }
    }
}

