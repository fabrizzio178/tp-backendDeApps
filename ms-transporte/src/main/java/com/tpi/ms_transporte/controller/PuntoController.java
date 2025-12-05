package com.tpi.ms_transporte.controller;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tpi.ms_transporte.entities.Punto;
import com.tpi.ms_transporte.services.PuntoService;

@RestController
@RequestMapping("/api/puntos")
public class PuntoController {
    private final PuntoService service;
    private final Logger log = LoggerFactory.getLogger(PuntoController.class);

    public PuntoController(PuntoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Punto> obtenerPuntos() {
        try {
            log.info("Obteniendo lista de puntos");
            return service.findAll();
        } catch (Exception e) {
            log.error("Error al obtener puntos: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @GetMapping("/{id}")
    public Punto obtenerPuntoPorId(@PathVariable Long id) {
        try {
            log.info("Obteniendo punto con ID: {}", id);
            return service.findById(id);
        } catch (Exception e) {
            log.error("Error al obtener punto por ID: {}", e.getMessage());
            return null;
        }
    }

    @PostMapping
    public Punto crearPunto(@RequestBody Punto punto) {
        try {
            log.info("Creando nuevo punto: {}", punto);
            return service.create(punto);
        } catch (Exception e) {
            log.error("Error al crear punto: {}", e.getMessage());
            return null;
        }
    }

    @PutMapping("/{id}")
    public Punto actualizarPunto(@PathVariable Long id, @RequestBody Punto punto) {
        try {
            log.info("Actualizando punto con ID: {}", id);
            return service.update(id, punto);
        } catch (Exception e) {
            log.error("Error al actualizar punto: {}", e.getMessage());
            return null;
        }
    }
}
