package com.tpi.ms_transporte.controller;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

import com.tpi.ms_transporte.entities.Tramo;
import com.tpi.ms_transporte.services.TramoService;

@RestController
@RequestMapping("/api/tramos")
public class TramoController {
    private final TramoService service;
    private final Logger log = LoggerFactory.getLogger(TramoController.class);


    public TramoController(TramoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Tramo> obtenerTramos() {
        try {
            log.info("Obteniendo lista de tramos");
            return service.findAll();
        } catch (Exception e) {
            log.error("Error al obtener tramos: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @GetMapping("/{id}")
    public Tramo obtenerTramoPorId(@PathVariable Long id) {
        try {
            log.info("Obteniendo tramo con ID: {}", id);
            return service.findById(id);
        } catch (Exception e) {
            log.error("Error al obtener tramo por ID: {}", e.getMessage());
            return null;
        }
    }

    @GetMapping("/{id}/estado")
    public String obtenerEstadoTramo(@PathVariable Long id) {
        try {
            Tramo tramo = service.findById(id);
            log.info("Obteniendo estado del tramo con ID: {}", id);
            if (tramo != null && tramo.getEstado() != null) {
                log.info("Estado del tramo: {}", tramo.getEstado().getNombre());
                return tramo.getEstado().getNombre();
            }
            log.info("Tramo no encontrado con ID: {}", id);
            return "Tramo no encontrado";
        } catch (Exception e) {
            log.error("Error al obtener estado del tramo: {}", e.getMessage());
            return "Error al obtener estado";
        }
    }

    @PostMapping
    public Tramo crearTramo(@RequestBody Tramo tramo) {
        try {
            log.info("Creando nuevo tramo: {}", tramo);
            return service.create(tramo);
        } catch (Exception e) {
            log.error("Error al crear tramo: {}", e.getMessage());
            return null;
        }
    }

    @PutMapping("/{id}")
    public Tramo actualizarTramo(@PathVariable Long id, @RequestBody Tramo tramo) {
        try {
            log.info("Actualizando tramo con ID: {}", id);
            return service.update(id, tramo);
        } catch (Exception e) {
            log.error("Error al actualizar tramo: {}", e.getMessage());
            return null;
        }
    }

    @PostMapping("/{id}/camion")
    public Tramo asignarCamionPost(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            log.info("Asignando camión al tramo con ID: {}", id);
            String dominio = body == null ? null : body.getOrDefault("dominioCamion", body.get("dominio"));
            return service.asignarCamion(id, dominio);
        } catch (Exception e) {
            log.error("Error al asignar camión al tramo: {}", e.getMessage());
            return null;
        }
    }

    @PutMapping("/{id}/camion")
    public Tramo asignarCamionPut(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String dominio = body == null ? null : body.getOrDefault("dominioCamion", body.get("dominio"));
            log.info("Asignando camión al tramo con ID: {}", id);
            return service.asignarCamion(id, dominio);
        } catch (Exception e) {
            log.error("Error al asignar camión al tramo: {}", e.getMessage());
            return null;
        }
    }

    @PostMapping("/{id}/inicio")
    public Tramo iniciarTramo(@PathVariable Long id) {
        try {
            log.info("Iniciando tramo con ID: {}", id);
            return service.registrarInicioFin(id, "inicio");
        } catch (Exception e) {
            log.error("Error al iniciar tramo: {}", e.getMessage());
            return null;
        }
    }

    @PostMapping("/{id}/fin")
    public Tramo finalizarTramo(@PathVariable Long id) {
        try {
            log.info("Finalizando tramo con ID: {}", id);
            return service.registrarInicioFin(id, "fin");
        } catch (Exception e) {
            log.error("Error al finalizar tramo: {}", e.getMessage());
            return null;
        }
    }

    @GetMapping("/{id}/tiempo-real")
    public Map<String, Object> obtenerTiempoReal(@PathVariable Long id) {
        try {
            log.info("Obteniendo tiempo real del tramo con ID: {}", id);
            return service.calcularTiempoReal(id);
        } catch (Exception e) {
            log.error("Error al calcular tiempo real del tramo: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    @GetMapping("/{id}/costo-real")
    public Map<String, Object> obtenerCostoReal(@PathVariable Long id) {
        try {
            log.info("Obteniendo costo real del tramo con ID: {}", id);
            return service.calcularCostoReal(id);
        } catch (Exception e) {
            log.error("Error al calcular costo real del tramo: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

        @GetMapping("/ruta/{idRuta}")
    public List<Tramo> obtenerTramosPorRuta(@PathVariable Long idRuta) {
        try {
            log.info("Obteniendo tramos por ruta con ID: {}", idRuta);
            return service.findByRuta(idRuta);
        } catch (Exception e) {
            log.error("Error al obtener tramos por ruta: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

}
