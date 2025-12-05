package com.tpi.ms_transporte.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tpi.ms_transporte.entities.Ruta;
import com.tpi.ms_transporte.repository.RutaRepository;

@Service
public class RutaService {
    private final RutaRepository repo;
    private static final Logger log = LoggerFactory.getLogger(RutaService.class);

    public RutaService(RutaRepository repo) {
        this.repo = repo;
    }

    public List<Ruta> findAll() {
        log.info("Obteniendo todas las rutas desde la base de datos.");
        return repo.findAll();
    }

    public Ruta findById(Long id) {
        log.info("Obteniendo ruta con ID: {} desde la base de datos.", id);
        return repo.findById(id).orElse(null);
    }

    @Transactional
    public Ruta create(Ruta ruta) {
        log.info("Creando una nueva ruta en la base de datos.");
        return repo.save(ruta);
    }

    @Transactional
    public Ruta update(Long id, Ruta ruta) {
        log.info("Actualizando ruta con ID: {} en la base de datos.", id);
        Ruta existing = repo.findById(id).orElse(null);
        if (existing == null) {
            log.error("Ruta con ID: {} no encontrada para actualizar.", id);
            return null;
        }
        existing.setIdDeposito(ruta.getIdDeposito());
        log.info("Ruta con ID: {} actualizada exitosamente.", id);
        return repo.save(existing);

    }

}
