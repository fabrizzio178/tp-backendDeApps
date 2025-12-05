package com.tpi.microcontenedores.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tpi.microcontenedores.dto.ClienteDTO;
import com.tpi.microcontenedores.entities.Contenedor;
import com.tpi.microcontenedores.entities.Estado;
import com.tpi.microcontenedores.services.ContenedorService;

@RestController
@RequestMapping("/api/contenedores")
public class ContenedorController {
    private final ContenedorService service;
    private static final Logger log = LoggerFactory.getLogger(ContenedorController.class);

    public ContenedorController(ContenedorService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Contenedor>> obtenerContenedores(){
        try{
            log.info("Iniciando obtención de todos los contenedores");
            ResponseEntity<List<Contenedor>> response = service.findAll();
            log.info("Contenedores obtenidos exitosamente.");
            return response;
        } catch (Exception e) {
            log.error("Error al obtener contenedores: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contenedor> obtenerContenedorPorId(@PathVariable Long id){
        try{
            log.info("Iniciando obtención de contenedor por ID: {}", id);
            return service.findById(id);
        } catch (Exception e) {
            log.error("Error al obtener contenedor por ID: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("{id}/estado")
    public ResponseEntity<Estado> obtenerEstadoContenedor(@PathVariable Long id){
        try{
            log.info("Iniciando obtención de estado del contenedor con ID: {}", id);
            return service.getEstadoById(id);

        } catch (Exception e) {
            log.error("Error al obtener estado del contenedor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping(params = "estado")
    public ResponseEntity<List<Contenedor>> obtenerContenedoresPorEstado(@RequestParam String estado){
        try{
            log.info("Iniciando obtención de contenedores por estado: {}", estado);
            return service.consultarEstadoPendiente(estado);
        } catch (Exception e) {
            log.error("Error al obtener contenedores por estado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/clientes")
    public ResponseEntity<List<ClienteDTO>> obtenerClientes() {
        try{
            log.info("Iniciando obtención de clientes desde microservicio de usuarios");
            return service.obtenerClientesDesdeUsuarios();
        } catch (Exception e){
            log.error("Error al obtener clientes desde microservicio de usuarios: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping()
    public ResponseEntity<Contenedor> registrarContenedor(@RequestBody Contenedor contenedor){
        try{
            log.info("Iniciando registro de un nuevo contenedor");
            return service.registrarContenedor(contenedor);
        } catch (Exception e) {
            log.error("Error al registrar contenedor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Void> actualizarEstadoContenedor(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String nuevoEstado = body != null ? body.get("estado") : null;
            log.info("Actualizando estado del contenedor {} a {}", id, nuevoEstado);
            return service.actualizarEstado(id, nuevoEstado);
        } catch (Exception e) {
            log.error("Error al actualizar estado del contenedor {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
