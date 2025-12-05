package com.tpi.microcontenedores.services;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.tpi.microcontenedores.dto.ClienteDTO;
import com.tpi.microcontenedores.entities.Contenedor;
import com.tpi.microcontenedores.entities.Estado;
import com.tpi.microcontenedores.repository.ContenedorRepository;
import com.tpi.microcontenedores.repository.EstadoRepository;

@Service
public class ContenedorService {
    private final ContenedorRepository repo;
    private final EstadoRepository estadoRepository;
    private final RestTemplate restTemplate;
    private static final String TIPO_ENTIDAD_CONTENEDOR = "contenedor";
    private static final Logger log = LoggerFactory.getLogger(ContenedorService.class);

    public ContenedorService(ContenedorRepository repo, EstadoRepository estadoRepository, RestTemplate restTemplate) {
        this.repo = repo;
        this.estadoRepository = estadoRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public ResponseEntity<Void> actualizarEstado(Long idContenedor, String nuevoEstado) {
        if (idContenedor == null || nuevoEstado == null || nuevoEstado.isBlank()) {
            log.warn("Petición inválida para actualizar estado de contenedor: id={}, estado={}", idContenedor, nuevoEstado);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Contenedor contenedor = repo.findById(idContenedor).orElse(null);
        if (contenedor == null) {
            log.warn("Contenedor con ID {} no encontrado para actualizar estado", idContenedor);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Estado estado = buscarEstadoPorNombre(nuevoEstado, TIPO_ENTIDAD_CONTENEDOR);
        if (estado == null) {
            log.warn("Estado '{}' no encontrado para la entidad contenedor", nuevoEstado);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        contenedor.setEstado(estado);
        repo.save(contenedor);
        log.info("Estado del contenedor {} actualizado a {}", idContenedor, nuevoEstado);
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<List<Contenedor>> findAll(){
        log.info("Obteniendo todos los contenedores desde la base de datos.");
        List<Contenedor> contenedores = repo.findAll();
        try{
            if(contenedores.isEmpty()){
                log.info("No se encontraron contenedores en la base de datos.");
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(contenedores);
        } catch (Exception e){
            log.error("Error al obtener contenedores: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<Contenedor> findById(Long id){
        log.info("Obteniendo contenedor con ID: {}", id);
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
        
    }

    public ResponseEntity<Estado> getEstadoById(Long id){
        log.info("Obteniendo estado del contenedor con ID: {}", id);
        Contenedor contenedor = repo.findById(id).orElse(null);
        if(contenedor != null){
            log.info("Estado del contenedor con ID {}: {}", id, contenedor.getEstado());
            return ResponseEntity.ok(contenedor.getEstado());
        }
        log.warn("Contenedor con ID {} no encontrado.", id);
        return ResponseEntity.notFound().build();
    }

    public ResponseEntity<List<Contenedor>> consultarEstadoPendiente(String nombre){
        try{
            log.info("Consultando contenedores con estado pendiente: {}", nombre);
            List<Contenedor> pendientes = repo.findByEstadoNombre(nombre);
            if(pendientes.isEmpty()){
                log.info("No se encontraron contenedores con estado pendiente: {}", nombre);
                return ResponseEntity.noContent().build();
            }
            log.info("Contenedores con estado pendiente encontrados: {}", pendientes.size());
            return ResponseEntity.ok(pendientes);
        } catch(Exception e){
            log.error("Error al consultar contenedores por estado pendiente: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<Contenedor> registrarContenedor(Contenedor contenedor){
        log.info("Registrando nuevo contenedor: {}", contenedor);
        if(contenedor == null){
            log.warn("Intento de registrar un contenedor nulo. Debes enviar un contenedor en el body.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try{
            // Resolver estado si vino idEstado en el body
            if ((contenedor.getEstado() == null || contenedor.getEstado().getId() == null)
                    && contenedor.getIdEstado() != null) {
                        log.info("Resolviendo estado para idEstado: {}", contenedor.getIdEstado());
                Estado estado = estadoRepository.findById(contenedor.getIdEstado().longValue())

                        .orElseThrow(() -> new IllegalArgumentException("Estado id=" + contenedor.getIdEstado() + " no existe"));
                contenedor.setEstado(estado);
            }

            // Validaciones mínimas (opcional: reforzar según tu dominio)
            if (contenedor.getIdCliente() == null) {
                log.warn("Intento de registrar un contenedor sin idCliente.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            if (contenedor.getEstado() == null || contenedor.getEstado().getId() == null) {
                log.warn("Intento de registrar un contenedor sin estado válido.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            Contenedor saved = repo.save(contenedor);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            log.warn("Error de datos invalidos al registrar contenedor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (DataIntegrityViolationException e){
            log.warn("Violacion de integridad de datos al registrar contenedor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e){
            log.error("Error al registrar contenedor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Metodo de prueba para probar comunicacion con microservicio de clientes
    public ResponseEntity<List<ClienteDTO>> obtenerClientesDesdeUsuarios() {
        String url = "http://ms-usuarios:8083/api/clientes"; // URL del microservicio de usuarios
        try {
            ResponseEntity<ClienteDTO[]> response = restTemplate.getForEntity(url, ClienteDTO[].class);
            if (response.getStatusCode() == HttpStatus.OK) {
                ClienteDTO[] clientesArray = response.getBody();
                List<ClienteDTO> clientes = List.of(clientesArray);
                return ResponseEntity.ok(clientes);
            } else {
                return ResponseEntity.status(response.getStatusCode()).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private Estado buscarEstadoPorNombre(String nombre, String tipoEntidad) {
        if (nombre == null || tipoEntidad == null) {
            return null;
        }

        return estadoRepository
                .findByNombreIgnoreCaseAndTipoEntidadIgnoreCase(nombre, tipoEntidad)
                .orElseGet(() -> estadoRepository.findByTipoEntidadIgnoreCase(tipoEntidad).stream()
                        .filter(estado -> normalizar(estado.getNombre()).equals(normalizar(nombre)))
                        .findFirst()
                        .orElse(null));
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        String sinTildes = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return sinTildes.trim().toLowerCase(Locale.ROOT);
    }
}
