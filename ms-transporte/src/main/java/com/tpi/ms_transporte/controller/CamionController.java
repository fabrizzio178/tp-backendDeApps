package com.tpi.ms_transporte.controller;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tpi.ms_transporte.entities.Camion;
import com.tpi.ms_transporte.services.CamionService;

@RestController
@RequestMapping("/api/camiones")
public class CamionController {
    private final CamionService service;
    private final Logger log = LoggerFactory.getLogger(CamionController.class);

    public CamionController(CamionService service) {
        this.service = service;
    }

    @GetMapping
    public List<Camion> obtenerCamiones() {
        try {
            log.info("Obteniendo lista de camiones");
            return service.findAll();
        } catch (Exception e) {
            log.error("Error al obtener camiones: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @GetMapping("/{dominio}")
    public Camion obtenerCamion(@PathVariable String dominio) {
        try {
            log.info("Obteniendo camión con dominio: {}", dominio);
            return service.findByDominio(dominio);
        } catch (Exception e) {
            log.error("Error al obtener camión: {}", e.getMessage());
            return null;
        }
    }

    @PostMapping
    public ResponseEntity<Camion> registrarCamion(@RequestBody Camion camion) {
        try {
            log.info("Registrando camión: {}", camion);
            return ResponseEntity.ok(service.create(camion));
        } catch (Exception e) {
            log.error("Error al crear camión: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{dominio}")
    public ResponseEntity<Camion> actualizarCamion(@PathVariable String dominio, @RequestBody Camion camion) {
        try {
            log.info("Actualizando camión con dominio: {}", dominio);
            return service.update(camion);
        } catch (Exception e) {
            log.error("Error al actualizar camión: {}", e.getMessage());
            return null;
        }
    }

    @PutMapping("/{dominio}/asignar-contenedor/{idContenedor}")
    public Camion asignarContenedor(@PathVariable String dominio, @PathVariable Long idContenedor) {
        try {
            log.info("Asignando contenedor {} al camión con dominio: {}", idContenedor, dominio);
            return service.asignarContenedor(dominio, idContenedor);
        } catch (IllegalArgumentException ex) {
            log.error("Error al asignar contenedor: {}", ex.getMessage());
            throw ex; // se propaga 400 por defecto si hay controlador de excepciones, de lo contrario 500
        } catch (Exception e) {
            log.error("Error al asignar contenedor: {}", e.getMessage());
            return null;
        }
    }

    @PutMapping("/{dominio}/desasignar-contenedor")
    public Camion desasignarContenedor(@PathVariable String dominio) {
        try {
            log.info("Desasignando contenedor del camión con dominio: {}", dominio);
            return service.asignarContenedor(dominio, null);
        } catch (Exception e) {
            log.error("Error al desasignar contenedor: {}", e.getMessage());
            return null;
        }
    }

    @GetMapping("/{dominio}/validar")
    public boolean validarCapacidad(@PathVariable String dominio, @RequestParam("idContenedor") Long idContenedor) {
        try {
            log.info("Validando capacidad del camión con dominio: {} para el contenedor con ID: {}", dominio, idContenedor);
            return service.validarCapacidadParaContenedor(dominio, idContenedor);
        } catch (Exception e) {
            log.error("Error al validar capacidad del camión: {}", e.getMessage());
            return false;
        }
    }
}
