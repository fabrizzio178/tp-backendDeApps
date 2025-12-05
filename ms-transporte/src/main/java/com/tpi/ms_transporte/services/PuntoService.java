package com.tpi.ms_transporte.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tpi.ms_transporte.entities.Punto;
import com.tpi.ms_transporte.repository.PuntoRepository;

@Service
public class PuntoService {
    private final PuntoRepository repo;
    private static final Logger log = LoggerFactory.getLogger(PuntoService.class);

    public PuntoService(PuntoRepository repo) {
        this.repo = repo;
    }

    public List<Punto> findAll() {
        log.info("Obteniendo todos los puntos desde la base de datos.");
        return repo.findAll();
    }

    public Punto findById(Long id) {
        log.info("Obteniendo punto con ID: {} desde la base de datos.", id);    
        return repo.findById(id).orElse(null);
    }

    @Transactional
    public Punto create(Punto punto) {
        log.info("Creando un nuevo punto en la base de datos.");
        return repo.save(punto);
    }

    @Transactional
    public Punto update(Long id, Punto punto) {
        log.info("Actualizando punto con ID: {} en la base de datos.", id);
        Punto existing = repo.findById(id).orElse(null);
        if (existing == null) return null;
        existing.setTipoPunto(punto.getTipoPunto());
        existing.setLongitud(punto.getLongitud());
        existing.setLatitud(punto.getLatitud());
        existing.setIdCiudad(punto.getIdCiudad());
        existing.setIdDeposito(punto.getIdDeposito());
        log.info("Punto con ID: {} actualizado exitosamente.", id);
        return repo.save(existing);
    }

    @Transactional
    public Punto findByIdDeposito(Long idDeposito) {
        log.info("Buscando punto con ID de depósito: {} en la base de datos.", idDeposito);
        List<Punto> puntos = repo.findByIdDeposito(idDeposito);
        log.info("Encontrados {} puntos con ID de depósito: {}.", puntos.size(), idDeposito);
        return puntos.isEmpty() ? null : puntos.get(0);
    }

    @Transactional
    public Punto findOrCreatePuntoPorCoords(double lat, double lon, String tipo) {
        log.info("Buscando o creando punto con latitud: {}, longitud: {} y tipo: {} en la base de datos.", lat, lon, tipo);
        List<Punto> existentes = repo.findByLatitudAndLongitud(lat, lon);
        if (!existentes.isEmpty()) return existentes.get(0);

        Punto nuevo = new Punto();
        nuevo.setLatitud(lat);
        nuevo.setLongitud(lon);
        nuevo.setTipoPunto(tipo);
        log.info("Creando nuevo punto con latitud: {}, longitud: {} y tipo: {} en la base de datos.", lat, lon, tipo);
        return repo.save(nuevo);
    }


}
