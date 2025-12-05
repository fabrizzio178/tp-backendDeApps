package com.tpi.ms_transporte.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tpi.ms_transporte.entities.Ruta;
import com.tpi.ms_transporte.services.RutaService;
import com.tpi.ms_transporte.services.TramoService;

@RestController
@RequestMapping("/api/rutas")
public class RutaController {
    private final RutaService service;
    private final TramoService tramoService;
    private final Logger log = LoggerFactory.getLogger(RutaController.class);


    public RutaController(RutaService service, TramoService tramoService) {
        this.service = service;
        this.tramoService = tramoService;
    }

    @GetMapping
    public List<Ruta> obtenerRutas() {
        try {
            log.info("Obteniendo lista de rutas");
            return service.findAll();
        } catch (Exception e) {
            log.error("Error al obtener rutas: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @GetMapping("/{id}")
    public Ruta obtenerRutaPorId(@PathVariable Long id) {
        try {
            log.info("Obteniendo ruta con ID: {}", id);
            return service.findById(id);
        } catch (Exception e) {
            log.error("Error al obtener ruta por ID: {}", e.getMessage());
            return null;
        }
    }

    @PostMapping
    public Ruta crearRuta(@RequestBody Ruta ruta) {
        try {
            log.info("Creando nueva ruta: {}", ruta);
            return service.create(ruta);
        } catch (Exception e) {
            log.error("Error al crear ruta: {}", e.getMessage());
            return null;
        }
    }

    @PutMapping("/{id}")
    public Ruta actualizarRuta(@PathVariable Long id, @RequestBody Ruta ruta) {
        try {
            log.info("Actualizando ruta con ID: {}", id);
            return service.update(id, ruta);
        } catch (Exception e) {
            log.error("Error al actualizar ruta: {}", e.getMessage());
            return null;
        }
    }

    @GetMapping("ruta-tentativa/{idRuta}")
    public Map<String, Object> obtenerRutaTentativa(@PathVariable Long idRuta) {
        try {
            log.info("Calculando ruta tentativa para ID de ruta: {}", idRuta);
            return tramoService.calcularRutaTentativa(idRuta);
        } catch (Exception e) {
            log.error("Error al calcular ruta tentativa: {}", e.getMessage());
            return java.util.Collections.emptyMap();
        }
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/calcular")
    public Map<String, Object> calcularRutaTentativa(@RequestBody Map<String, Object> body) {
        try {
            Map<String, Object> coordenadas = body;
            Object nested = body.get("coordenadas");
            if (nested instanceof Map<?, ?> nestedMap) {
                coordenadas = (Map<String, Object>) nestedMap;
            }

            Double latOrigen = extractDouble(coordenadas, "latOrigen");
            Double lonOrigen = extractDouble(coordenadas, "lonOrigen");
            Double latDestino = extractDouble(coordenadas, "latDestino");
            Double lonDestino = extractDouble(coordenadas, "lonDestino");

            if (latOrigen == null || lonOrigen == null || latDestino == null || lonDestino == null) {
                log.warn("Solicitud inválida: faltan coordenadas. Payload recibido: {}", body);
                return Map.of(
                        "error", "Faltan coordenadas obligatorias",
                        "camposEsperados", List.of("latOrigen", "lonOrigen", "latDestino", "lonDestino")
                );
            }

            log.info("Calculando ruta tentativa desde ({}, {}) hasta ({}, {})", latOrigen, lonOrigen, latDestino, lonDestino);
            return tramoService.calcularRutaTentativa(latOrigen, lonOrigen, latDestino, lonDestino);
        } catch (Exception e) {
            log.error("Error al calcular ruta tentativa", e);
            return Map.of(
                    "error", "No se pudo calcular la ruta",
                    "detalle", e.getMessage()
            );
        }
    }

    private Double extractDouble(Map<String, Object> source, String key) {
        if (source == null || !source.containsKey(key)) {
            return null;
        }

        Object value = source.get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }

        if (value instanceof String text && !text.isBlank()) {
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException ex) {
                log.warn("No se pudo parsear el valor '{}' para la clave '{}'", text, key);
                return null;
            }
        }

        return null;
    }

}