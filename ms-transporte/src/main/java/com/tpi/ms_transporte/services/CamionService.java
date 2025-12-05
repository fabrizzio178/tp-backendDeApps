package com.tpi.ms_transporte.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tpi.ms_transporte.entities.Camion;
import com.tpi.ms_transporte.repository.CamionRepository;
import com.tpi.ms_transporte.dto.ContenedorDTO;

@Service
public class CamionService {
    private final CamionRepository repo;
    private final RestTemplate restTemplate;
    private final Logger log = LoggerFactory.getLogger(CamionService.class);

    @Value("${services.contenedores.base-url:http://ms-contenedores:8081}")
    private String contenedoresBaseUrl;

    public CamionService(CamionRepository repo, JdbcTemplate jdbc, RestTemplate restTemplate) {
        this.repo = repo;
        this.restTemplate = restTemplate;
    }

    @Transactional(readOnly = true)
    public List<Camion> findAll() {
        log.info("Obteniendo todos los camiones desde la base de datos.");
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public Camion findByDominio(String dominio) {
        log.info("Obteniendo camión con dominio: {} desde la base de datos.", dominio);
        return repo.findById(dominio).orElse(null);
    }

    @Transactional
    public Camion create(Camion c) {
        if(c != null){
            log.info("Creando un nuevo camión con dominio: {} en la base de datos.", c.getDominioCamion());
            return repo.save(c);
        }
        log.warn("Intento de crear un camión nulo.");
        return null;
    }

    @Transactional
    public ResponseEntity<Camion> update(Camion c) {
        Camion existing = repo.findById(c.getDominioCamion()).orElse(null);
        log.info("Actualizando camión con dominio: {} en la base de datos.", c.getDominioCamion());
        if (existing == null) {
            log.error("Camión con dominio: {} no encontrado para actualizar.", c.getDominioCamion());
            return ResponseEntity.notFound().build();
        }
        existing.setCapacidadPeso(c.getCapacidadPeso());
        existing.setCapacidadVolumen(c.getCapacidadVolumen());
        existing.setDisponibilidad(c.getDisponibilidad());
        existing.setConsumoCombustible(c.getConsumoCombustible());
        existing.setCostoBase(c.getCostoBase());
        existing.setIdTransportista(c.getIdTransportista());
        existing.setLatitud(c.getLatitud());
        existing.setLongitud(c.getLongitud());
        log.info("Actualizando camión con dominio: {} en la base de datos.", c.getDominioCamion());
        return ResponseEntity.ok(repo.save(existing));
    }

    @Transactional
    public Camion asignarContenedor(String dominio, Long idContenedor) {
        Camion camion = repo.findById(dominio).orElse(null);
        if (camion == null) {
            log.error("Camión con dominio: {} no encontrado para asignar contenedor.", dominio);
            return null;
        }
        if (idContenedor != null) {
            if (!validarCapacidadParaContenedor(dominio, idContenedor)) {
                log.error("El contenedor con ID: {} excede la capacidad de peso o volumen del camión con dominio: {}.", idContenedor, dominio);
                throw new IllegalArgumentException("El contenedor excede la capacidad de peso o volumen del camión");
            }
        }
        log.info("Asignando contenedor con ID: {} al camión con dominio: {}.", idContenedor, dominio);
        camion.setIdContenedor(idContenedor);
        return repo.save(camion);
    }

    @Transactional(readOnly = true)
    public boolean validarCapacidadParaContenedor(String dominio, Long idContenedor) {
        Camion camion = repo.findById(dominio).orElse(null);
        if (camion == null) {
            log.error("Camión con dominio: {} no encontrado para validar capacidad.", dominio);
            return false;
        }
        if (idContenedor == null) return true;

        try {
            String url = contenedoresBaseUrl + "/api/contenedores/" + idContenedor;
            ContenedorDTO dto = restTemplate.getForObject(url, ContenedorDTO.class);
            if (dto == null) return false;

            BigDecimal peso = dto.getPeso() == null ? BigDecimal.ZERO : dto.getPeso();
            BigDecimal volumen = dto.getVolumen() == null ? BigDecimal.ZERO : dto.getVolumen();

            BigDecimal capPeso = camion.getCapacidadPeso();
            BigDecimal capVol = camion.getCapacidadVolumen();

            boolean dentroPeso = (capPeso == null) || peso.compareTo(capPeso) <= 0;
            boolean dentroVolumen = (capVol == null) || volumen.compareTo(capVol) <= 0;
            log.info("Validando capacidad para contenedor con ID: {} en camión con dominio: {}. Dentro de peso: {}, Dentro de volumen: {}", idContenedor, dominio, dentroPeso, dentroVolumen);
            return dentroPeso && dentroVolumen;
        } catch (Exception ex) {
            log.error("Error al validar capacidad para contenedor con ID: {} en camión con dominio: {}.", idContenedor, dominio, ex);
            return false;
        }
    }

}
