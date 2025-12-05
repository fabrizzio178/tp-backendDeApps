package com.tpi.microcontenedores.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tpi.microcontenedores.dto.SolicitudAsignacionRutaResponseDTO;
import com.tpi.microcontenedores.dto.SolicitudRequestDTO;
import com.tpi.microcontenedores.dto.SolicitudResponseDTO;
import com.tpi.microcontenedores.services.SolicitudService;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudController {

    private final SolicitudService service;
    private static final Logger log = LoggerFactory.getLogger(SolicitudController.class);

    public SolicitudController(SolicitudService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<SolicitudResponseDTO>> obtenerSolicitudes() {
        try {
            log.info("Recibiendo solicitud para obtener todas las solicitudes");
            log.debug("Llamando al servicio SolicitudService.obtenerSolicitudes()");
            return service.obtenerSolicitudes();
        } catch (Exception e) {
            log.error("Error al obtener solicitudes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitudResponseDTO> obtenerSolicitudPorId(@PathVariable Long id) {
        try {
            log.info("Recibiendo solicitud para obtener solicitud por ID: {}", id);
            log.debug("Llamando al servicio SolicitudService.obtenerSolicitudPorId()");
            return service.obtenerSolicitudPorId(id);
        } catch (Exception e) {
            log.error("Error al obtener solicitud por ID: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<SolicitudResponseDTO> registrarSolicitud(@RequestBody SolicitudRequestDTO request, @RequestHeader(value="Authorization", required=false) String token) {
        try {
            log.info("Recibiendo solicitud para registrar una nueva solicitud");
            log.debug("Llamando al servicio SolicitudService.registrarSolicitud()");
            return service.registrarSolicitud(request, token);
        } catch (Exception e) {
            log.error("Error al registrar solicitud: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{idSolicitud}/ruta/{idRuta}")
    public ResponseEntity<SolicitudAsignacionRutaResponseDTO> asignarRuta(@PathVariable Long idSolicitud,
                                                                          @PathVariable Long idRuta,
                                                                          @RequestHeader(value="Authorization", required=false) String token) {
        try {
            log.info("Recibiendo solicitud de asignación de ruta {} a la solicitud {}", idRuta, idSolicitud);
            log.debug("Llamando al servicio SolicitudService.asignarRuta()");
            return service.asignarRuta(idSolicitud, idRuta, token);
        } catch (Exception e) {
            log.error("Error al asignar ruta {} a la solicitud {}: {}", idRuta, idSolicitud, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/calculo-real")
    public ResponseEntity<SolicitudResponseDTO> registrarCalculoReal(@PathVariable Long id, @RequestHeader(value="Authorization", required=false) String token) {
        try {
            return service.registrarCostosReales(id, token);
        } catch (Exception e) {
            System.out.println("\nError al registrar costo/tiempo real\n: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/estado")
    public ResponseEntity<Void> actualizarEstadoPorRuta(@RequestBody Map<String, Object> body) {
        try {
            if (body == null) return ResponseEntity.badRequest().build();

            Object idRutaObj = body.get("idRuta");
            Object estadoObj = body.get("estado");

            if (idRutaObj == null || estadoObj == null) {
                return ResponseEntity.badRequest().build();
            }

            Integer idRuta = Integer.valueOf(idRutaObj.toString());
            String estado = estadoObj.toString();

            boolean actualizado = service.actualizarEstadoPorRuta(idRuta, estado);
            return actualizado ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            System.out.println("\nError al actualizar estado de la solicitud\n: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarSolicitud(@PathVariable Long id) {
        try{
            log.info("Recibiendo solicitud para eliminar solicitud con ID: {}", id);
            log.debug("Llamando al servicio SolicitudService.eliminarSolicitud()");
            return service.eliminarSolicitud(id);
        } catch (Exception e) {
            log.error("Error al eliminar solicitud: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/tarifa/{idTarifa}")
    public ResponseEntity<SolicitudResponseDTO> asignarTarifa(@PathVariable Long id, @PathVariable Long idTarifa, @RequestHeader(value="Authorization", required=false) String token) {
        try {
            return service.asignarTarifa(id, idTarifa);
        } catch (Exception e) {
            System.out.println("\nError al asignar tarifa a la solicitud\n: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
