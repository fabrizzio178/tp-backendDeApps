package com.tpi.ms_usuarios.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tpi.ms_usuarios.entities.Cliente;
import com.tpi.ms_usuarios.repository.ClienteRepository;

@Service
public class ClienteService {
    private final ClienteRepository repo;
    private static final Logger log = LoggerFactory.getLogger(ClienteService.class);

    public ClienteService(ClienteRepository repo) {
        this.repo = repo;
    }

    public List<Cliente> findAll() {
        log.info("Obteniendo todos los clientes");
        return repo.findAll();
    }

    public Cliente findById(Long id) {
        try{
            log.info("Buscando cliente con ID: {}", id);
            Cliente cliente = repo.findById(id).orElse(null);
            if (cliente != null) {
                log.info("Cliente con ID: {} encontrado", id);
            } else {
                log.warn("Cliente con ID: {} no encontrado", id);
            }
        } catch (Exception e){
            log.error("Error al buscar cliente con ID {}: {}", id, e.getMessage());
        }
        return repo.findById(id).orElse(null);
    }

    @Transactional
    public Cliente create(Cliente c) {
        log.info("Creando un nuevo cliente");
        return repo.save(c);
    }

    @Transactional
    public Cliente update(Long id, Cliente c) {
        try{
            log.info("Actualizando cliente con ID: {}", id);
            Cliente existing = repo.findById(id).orElse(null);
            if (existing == null) return null;
            existing.setNombre(c.getNombre());
            existing.setApellido(c.getApellido());
            existing.setDni(c.getDni());
            existing.setMail(c.getMail());
            existing.setNumero(c.getNumero());
            log.info("Cliente con ID: {} actualizado exitosamente", id);
            return repo.save(existing);
        } catch (Exception e){
            log.error("Error al actualizar cliente con ID {}: {}", id, e.getMessage());
            return null;
        }
    }
}

