package com.tpi.ms_transporte.controller;

import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tpi.ms_transporte.entities.Deposito;
import com.tpi.ms_transporte.services.DepositoService;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/depositos")
public class DepositoController {
    private final DepositoService service;
    private final org.slf4j.Logger log = LoggerFactory.getLogger(DepositoController.class);
    public DepositoController(DepositoService service) {
        this.service = service;
    }
    @GetMapping
    public ResponseEntity<List<Deposito>> findAll(){
        log.info("Obteniendo lista de depósitos");
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Deposito obtenerPorId(@PathVariable Long id) {
        log.info("Obteniendo depósito con ID: {}", id);
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<Deposito> registrarDeposito(@RequestBody Deposito deposito){
        log.info("Registrando depósito: {}", deposito);
        return service.registrarDeposito(deposito);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Deposito> actualizarDeposito(@PathVariable Long id, @RequestBody Deposito deposito){
        log.info("Actualizando depósito con ID: {}", id);
        deposito.setId(id);
        return service.actualizarDeposito(deposito);
    }
}