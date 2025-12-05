package com.tpi.ms_usuarios.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import com.tpi.ms_usuarios.entities.Cliente;
import com.tpi.ms_usuarios.services.ClienteService;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteService service;
    private static final Logger log = LoggerFactory.getLogger(ClienteController.class);

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Cliente>> obtenerClientes() {
        try {
            log.info("Obteniendo todos los clientes");
            List<Cliente> clientes = service.findAll();
            if (clientes == null || clientes.isEmpty()) {
                log.warn("No se encontraron clientes");
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            log.error("Error al obtener clientes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> obtenerClientePorId(@PathVariable Long id) {
        try {
            log.info("Obteniendo cliente con ID: {}", id);
            Cliente cliente = service.findById(id);
            if (cliente == null) {
                log.warn("Cliente con ID: {} no encontrado", id);
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(cliente);
        } catch (Exception e) {
            log.error("Error al obtener cliente por ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Cliente> crearCliente(@RequestBody Cliente cliente) {
        try {
            log.info("Creando un nuevo cliente");
            if (cliente == null) {
                log.warn("Cliente proporcionado es nulo");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            Cliente created = service.create(cliente);
            log.info("Cliente creado con ID: {}", created.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("Error al crear cliente: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> actualizarCliente(@PathVariable Long id, @RequestBody Cliente cliente) {
        try {
            log.info("Actualizando cliente con ID: {}", id);
            Cliente updated = service.update(id, cliente);
            if (updated == null) {
                log.warn("Cliente con ID: {} no encontrado para actualizar", id);
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error al actualizar cliente con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
