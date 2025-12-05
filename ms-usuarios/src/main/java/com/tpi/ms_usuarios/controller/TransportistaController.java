package com.tpi.ms_usuarios.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tpi.ms_usuarios.entities.Transportista;
import com.tpi.ms_usuarios.services.TransportistaService;

@RestController
@RequestMapping("/api/transportistas")
public class TransportistaController {
    private final TransportistaService service;

    public TransportistaController(TransportistaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Transportista>> obtenerTransportistas() {
        try {
            List<Transportista> transportistas = service.findAll();
            if (transportistas == null || transportistas.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(transportistas);
        } catch (Exception e) {
            System.out.println("\nError al obtener transportistas\n: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transportista> obtenerTransportistaPorId(@PathVariable Long id) {
        try {
            Transportista t = service.findById(id);
            if (t == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(t);
        } catch (Exception e) {
            System.out.println("\nError al obtener transportista por ID\n: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Transportista> crearTransportista(@RequestBody Transportista transportista) {
        try {
            if (transportista == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            Transportista created = service.create(transportista);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            System.out.println("\nError al crear transportista\n: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transportista> actualizarTransportista(@PathVariable Long id, @RequestBody Transportista transportista) {
        try {
            Transportista updated = service.update(id, transportista);
            if (updated == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            System.out.println("\nError al actualizar transportista\n: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
