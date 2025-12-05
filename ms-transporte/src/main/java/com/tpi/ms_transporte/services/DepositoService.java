package com.tpi.ms_transporte.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tpi.ms_transporte.entities.Deposito;
import com.tpi.ms_transporte.repository.DepositoRepository;


@Service
public class DepositoService {
    private final DepositoRepository depositoRepository;
    private static final Logger log = LoggerFactory.getLogger(DepositoService.class);
    public DepositoService(DepositoRepository depositoRepository) {
        this.depositoRepository = depositoRepository;
    }

    @Transactional(readOnly = true)
    public Deposito findById(Long id) {
        log.info("Buscando depósito con ID: {} en la base de datos.", id);
        return depositoRepository.findById(id)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<List<Deposito>> findAll() {
        log.info("Obteniendo todos los depósitos desde la base de datos.");
        if(depositoRepository == null){
            log.warn("El repositorio de depósitos es nulo.");
            return ResponseEntity.noContent().build();
        }
        log.info("Encontrados {} depósitos en la base de datos.", depositoRepository.count());
        return ResponseEntity.ok(depositoRepository.findAll());
    }

    @Transactional
    public Deposito findByNombre(String nombre) {
        log.info("Buscando depósito con nombre: {} en la base de datos.", nombre);
        List<Deposito> lista = depositoRepository.findByNombre(nombre);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Transactional
    public ResponseEntity<Deposito> registrarDeposito(Deposito deposito){
        if(deposito != null){
            log.info("Registrando nuevo depósito: {} en la base de datos.", deposito.getNombre());
            return ResponseEntity.ok(depositoRepository.save(deposito));
        }
        log.warn("Intento de registrar depósito nulo.");
        return ResponseEntity.badRequest().build();
    }

    @Transactional
    public ResponseEntity<Deposito> actualizarDeposito(Deposito deposito){
        if(deposito != null && deposito.getId() != null){
            log.info("Actualizando depósito con ID: {} en la base de datos.", deposito.getId());
            Deposito existente = depositoRepository.findById(deposito.getId()).orElse(null);
            if(existente != null){
                existente.setNombre(deposito.getNombre());
                existente.setCostoEstadia(deposito.getCostoEstadia());
                existente.setLatitud(deposito.getLatitud());
                existente.setLongitud(deposito.getLongitud());
                existente.setDireccion(deposito.getDireccion());
                log.info("Depósito con ID: {} actualizado correctamente.", deposito.getId());
                return ResponseEntity.ok(depositoRepository.save(existente));
            }
        }
        log.warn("Depósito a actualizar no encontrado o nulo.");
        return ResponseEntity.notFound().build();
    }

}
