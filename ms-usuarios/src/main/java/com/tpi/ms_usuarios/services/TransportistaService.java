package com.tpi.ms_usuarios.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException.NotFound;

import com.tpi.ms_usuarios.entities.Transportista;
import com.tpi.ms_usuarios.repository.TransportistaRepository;

@Service
public class TransportistaService {
    private final TransportistaRepository repo;
    private static final Logger log = LoggerFactory.getLogger(TransportistaService.class);

    public TransportistaService(TransportistaRepository repo) {
        this.repo = repo;
    }

    public List<Transportista> findAll() {
        log.info("Obteniendo todos los transportistas");
        return repo.findAll();
    }

    public Transportista findById(Long id) {
        try{
            log.info("Buscando transportista con ID: {}", id);
            Transportista transportista = repo.findById(id).orElse(null);
            if (transportista != null) {
                log.info("Transportista con ID: {} encontrado", id);
            } else {
                log.warn("Transportista con ID: {} no encontrado", id);
            }
        } catch(NotFound e){
            log.error("Error al buscar transportista con ID {}: {}", id, e.getMessage());
        } catch(Exception e){
            log.error("Error inesperado al buscar transportista con ID {}: {}", id, e.getMessage());
        }
        return repo.findById(id).orElse(null);
    }

    @Transactional
    public Transportista create(Transportista t) {
        log.info("Creando un nuevo transportista");
        return repo.save(t);
    }

    @Transactional
    public Transportista update(Long id, Transportista t) {
        log.info("Actualizando transportista con ID: {}", id);
        Transportista existing = repo.findById(id).orElse(null);
        if (existing == null) log.error("Transportista con ID: {} no encontrado para actualizar", id);
        existing.setNombre(t.getNombre());
        existing.setTelefono(t.getTelefono());
        return repo.save(existing);
    }
}

