package com.tpi.microcontenedores.services;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tpi.microcontenedores.dto.ClienteDTO;
import com.tpi.microcontenedores.dto.ContenedorDTO;
import com.tpi.microcontenedores.dto.CoordenadasDTO;
import com.tpi.microcontenedores.dto.SolicitudAsignacionRutaResponseDTO;
import com.tpi.microcontenedores.dto.SolicitudRequestDTO;
import com.tpi.microcontenedores.dto.SolicitudResponseDTO;
import com.tpi.microcontenedores.dto.TransporteTramoDTO;
import com.tpi.microcontenedores.entities.Contenedor;
import com.tpi.microcontenedores.entities.Estado;
import com.tpi.microcontenedores.entities.Solicitud;
import com.tpi.microcontenedores.helpers.ClienteClient;
import com.tpi.microcontenedores.helpers.TarifaClient;
import com.tpi.microcontenedores.helpers.TransporteClient;
import com.tpi.microcontenedores.repository.ContenedorRepository;
import com.tpi.microcontenedores.repository.EstadoRepository;
import com.tpi.microcontenedores.repository.SolicitudRepository;

@Service
public class SolicitudService {
    private static final String DEFAULT_ESTADO_CONTENEDOR = "disponible";
    private static final String TIPO_ENTIDAD_CONTENEDOR = "contenedor";
    private static final Logger log = LoggerFactory.getLogger(SolicitudService.class);
    private static final String TIPO_ENTIDAD_SOLICITUD = "solicitud";
    private static final String ESTADO_SOLICITUD_PROGRAMADA = "programada";
    private static final String ESTADO_SOLICITUD_EN_TRANSITO = "en tránsito";
    private static final String ESTADO_SOLICITUD_ENTREGADA = "entregada";
    private static final String ESTADO_CONTENEDOR_EN_TRANSITO = "en tránsito";
    private static final String ESTADO_CONTENEDOR_ENTREGADO = "entregado";

    private final SolicitudRepository solicitudRepository;
    private final ContenedorRepository contenedorRepository;
    private final EstadoRepository estadoRepository;
    private final TransporteClient transporteClient;
    private final TarifaClient tarifaClient;
    private final ClienteClient clienteClient;

    public SolicitudService(SolicitudRepository solicitudRepository,
                           ContenedorRepository contenedorRepository,
                           EstadoRepository estadoRepository,
                           TransporteClient transporteClient,
                           TarifaClient tarifaClient,
                           ClienteClient clienteClient) {
        this.solicitudRepository = solicitudRepository;
        this.contenedorRepository = contenedorRepository;
        this.estadoRepository = estadoRepository;
        this.transporteClient = transporteClient;
        this.tarifaClient = tarifaClient;
        this.clienteClient = clienteClient;
    }

    @Transactional(readOnly = true)
    public ResponseEntity<List<SolicitudResponseDTO>> obtenerSolicitudes() {
        log.info("Inicando busqueda de todas las solicitudes");
        List<Solicitud> solicitudes = solicitudRepository.findAll();
        if (solicitudes.isEmpty()) {
            log.warn("No hay solicitudes registradas en la base de datos.");
            return ResponseEntity.noContent().build();
        }
        log.debug("Total de solicitudes encontradas: {}", solicitudes.size());
        List<SolicitudResponseDTO> dtos = solicitudes.stream()
            .map(this::mapToDTO)
            .toList();
        log.info("Busqueda de solicitudes completada exitosamente.");
        return ResponseEntity.ok(dtos);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<SolicitudResponseDTO> obtenerSolicitudPorId(Long id){
        try{
            return solicitudRepository.findById(id)
                .map(solicitud -> {
                    SolicitudResponseDTO dto = mapToDTO(solicitud);
                    return ResponseEntity.ok(dto);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Transactional
    public ResponseEntity<SolicitudResponseDTO> registrarSolicitud(SolicitudRequestDTO request, String authHeader) {

        log.info("Iniciando registro de una nueva solicitud. Cliente={}, Ruta={}", 
            request.getSolicitud().getIdCliente(),
            request.getCoordenadas().getIdRuta()
        );

        Solicitud solicitud = request.getSolicitud();
        CoordenadasDTO coordenadas = request.getCoordenadas();
        ClienteDTO clientePayload = request.getCliente();

        if (solicitud == null) {
            log.error("La solicitud proporcionada es nula.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if(solicitud.getIdCliente() != null && solicitud.getIdCliente() <= 0){
            log.error("El idCliente proporcionado no es válido: {}", solicitud.getIdCliente());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }


        try {
            Long resolvedClienteId = resolverCliente(solicitud, clientePayload, authHeader);
            if (resolvedClienteId != null) {
                solicitud.setIdCliente(resolvedClienteId.intValue());
            }

            // Fecha por si no viene
            if (solicitud.getFechaCreacion() == null) {
                solicitud.setFechaCreacion(LocalDateTime.now());
                log.info("Establecida fecha de creación {}", solicitud.getFechaCreacion());
            }

            // ==== Crear SIEMPRE un contenedor nuevo ====

            // Obtener estado "disponible" 
            Estado estadoDefault = estadoRepository
                    .findByNombreIgnoreCaseAndTipoEntidadIgnoreCase(
                            DEFAULT_ESTADO_CONTENEDOR,
                            TIPO_ENTIDAD_CONTENEDOR
                    )
                    .orElseThrow(() -> new IllegalStateException("Estado default no encontrado"));

            Contenedor nuevoContenedor = new Contenedor(); // Creamos el nuevo contenedor
            log.info("Creando un nuevo contenedor con valores predeterminados");
            nuevoContenedor.setPeso(BigDecimal.valueOf(100));
            nuevoContenedor.setAltura(BigDecimal.valueOf(2));
            nuevoContenedor.setAncho(BigDecimal.valueOf(2));
            nuevoContenedor.setLargo(BigDecimal.valueOf(6));
            BigDecimal volumen = nuevoContenedor.getAltura()
                    .multiply(nuevoContenedor.getAncho())
                    .multiply(nuevoContenedor.getLargo());
            nuevoContenedor.setVolumen(volumen);
            nuevoContenedor.setEstado(estadoDefault);

            // Poner el cliente que venga en la solicitud
            if (solicitud.getIdCliente() != null) {
                nuevoContenedor.setIdCliente(Long.valueOf(solicitud.getIdCliente()));
            }

            nuevoContenedor = contenedorRepository.save(nuevoContenedor);
            log.info("Nuevo contenedor guardado con ID: {}", nuevoContenedor.getId());
            solicitud.setContenedor(nuevoContenedor);
            log.info("Contenedor asignado a la solicitud con ID: {}", nuevoContenedor.getId());

            // === Obtener ruta tentativa ===
            Map<String, Object> rutaTentativa;
            if (coordenadas.getIdRuta() != null) {
                rutaTentativa = transporteClient.obtenerRutaTentativa(coordenadas.getIdRuta(), authHeader);
                log.info("Obtenida ruta tentativa para idRuta: {}", coordenadas.getIdRuta());
            } else {
                rutaTentativa = transporteClient.calcularRutasTentativas(coordenadas, authHeader);
                log.info("Calculadas rutas tentativas para coordenadas: {}", coordenadas);
            }

            if (rutaTentativa == null || rutaTentativa.isEmpty()) {
                log.warn("No se pudo obtener una ruta tentativa");
                throw new IllegalArgumentException("No se pudo obtener una ruta tentativa");
            }

            Integer idRutaSeleccionada = null;
            if (coordenadas.getIdRuta() != null) {
                idRutaSeleccionada = coordenadas.getIdRuta().intValue();
            } else {
                Object idRutaObj = rutaTentativa.get("idRuta");
                if (idRutaObj != null) {
                    idRutaSeleccionada = Integer.parseInt(idRutaObj.toString());
                }
            }

            if (idRutaSeleccionada == null) {
                Long ultimaRuta = transporteClient.obtenerUltimaRutaId();
                if (ultimaRuta != null) {
                    idRutaSeleccionada = ultimaRuta.intValue();
                }
            }

            if (idRutaSeleccionada != null) {
                solicitud.setIdRuta(idRutaSeleccionada);
                log.info("IdRuta asignado a la solicitud: {}", solicitud.getIdRuta());
            }

            procesarRutaTentativa(rutaTentativa, solicitud);

            // Completar datos obligatorios
            if (solicitud.getIdTarifa() == null) solicitud.setIdTarifa(null); 
            if (solicitud.getObservaciones() == null) solicitud.setObservaciones("Sin observaciones");

            if (solicitud.getEstado() == null)
                solicitud.setEstado(estadoRepository.findById(1L).orElseThrow());

            // === Guardar solicitud completa ===
            Solicitud solicitudGuardada = solicitudRepository.save(solicitud);
            log.info("Solicitud guardada exitosamente con ID: {}", solicitudGuardada.getId());
            SolicitudResponseDTO dto = mapToDTO(solicitudGuardada);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);

        } catch (IllegalArgumentException e) {
            log.warn("Error de datos invalidos al registrar solicitud: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (DataIntegrityViolationException e) {
            log.warn("Error de integridad de datos al registrar solicitud: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error inesperado al registrar la solicitud: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Transactional
    public ResponseEntity<SolicitudAsignacionRutaResponseDTO> asignarRuta(Long idSolicitud,
                                                                          Long idRuta,
                                                                          String authHeader) {
        log.info("Asignando ruta {} a la solicitud {}", idRuta, idSolicitud);

        if (idSolicitud == null || idRuta == null || idSolicitud <= 0 || idRuta <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(crearRespuestaAsignacion("Ids de solicitud y ruta deben ser válidos"));
        }

        Optional<Solicitud> solicitudOpt = solicitudRepository.findById(idSolicitud);
        if (solicitudOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(crearRespuestaAsignacion("La solicitud indicada no existe"));
        }

        Map<String, Object> ruta = transporteClient.obtenerRutaTentativa(idRuta, authHeader);
        if (ruta == null || ruta.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(crearRespuestaAsignacion("La ruta indicada no existe o no está disponible"));
        }

        Solicitud solicitud = solicitudOpt.get();
        solicitud.setIdRuta(idRuta.intValue());
        procesarRutaTentativa(ruta, solicitud);
        List<TransporteTramoDTO> tramos = obtenerTramosYActualizarEstado(solicitud, idRuta);
        Solicitud solicitudActualizada = solicitudRepository.save(solicitud);

        SolicitudAsignacionRutaResponseDTO response = new SolicitudAsignacionRutaResponseDTO();
        response.setSolicitud(mapToDTO(solicitudActualizada));
        response.setRuta(ruta);
        response.setTramos(tramos);
        response.setMensaje("Ruta asignada correctamente");

        return ResponseEntity.ok(response);
    }

    private Long resolverCliente(Solicitud solicitud, ClienteDTO clientePayload, String authHeader) {
        if (solicitud == null) {
            return null;
        }

        Integer idCliente = solicitud.getIdCliente();
        Long idClienteLong = (idCliente != null) ? idCliente.longValue() : null;

        if (idClienteLong != null) {
            Optional<ClienteDTO> existente = clienteClient.obtenerClientePorId(idClienteLong, authHeader);
            if (existente.isPresent()) {
                log.info("Cliente existente confirmado: {}", idClienteLong);
                return idClienteLong;
            }
            log.info("Cliente {} no encontrado en ms-usuarios", idClienteLong);
            if (clientePayload == null) {
                throw new IllegalArgumentException("El cliente especificado no existe y no se proporcionaron datos para crearlo");
            }
        }

        if (clientePayload != null) {
            log.info("Registrando nuevo cliente a través del microservicio de usuarios");
            ClienteDTO creado = clienteClient.crearCliente(clientePayload, authHeader);
            if (creado == null || creado.getId() == null) {
                throw new IllegalStateException("No se pudo registrar el cliente");
            }
            log.info("Cliente registrado con ID: {}", creado.getId());
            return creado.getId();
        }

        if (idClienteLong == null) {
            throw new IllegalArgumentException("Debe especificarse un cliente existente o los datos para crearlo");
        }

        return idClienteLong;
    }

    @Transactional
    public ResponseEntity<SolicitudResponseDTO> registrarCostosReales(Long idSolicitud, String token) {
        try {
            Solicitud solicitud = solicitudRepository.findById(idSolicitud).orElse(null);
            if (solicitud == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            if (solicitud.getIdRuta() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            Long idRuta = solicitud.getIdRuta().longValue();

            BigDecimal costoReal = tarifaClient.obtenerCostoRealRuta(idRuta, token);
            if (costoReal != null) {
                solicitud.setCostoFinal(costoReal.intValue());
            }

            Integer tiempoRealHoras = calcularTiempoRealRuta(solicitud);
            if (tiempoRealHoras != null) {
                solicitud.setTiempoReal(tiempoRealHoras);
            }

            Solicitud actualizada = solicitudRepository.save(solicitud);
            return ResponseEntity.ok(mapToDTO(actualizada));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Transactional
    public ResponseEntity<SolicitudResponseDTO> asignarTarifa(Long idSolicitud, Long idTarifa) {
        if (idSolicitud == null || idTarifa == null) {
            return ResponseEntity.badRequest().build();
        }

        Solicitud solicitud = solicitudRepository.findById(idSolicitud).orElse(null);
        if (solicitud == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        solicitud.setIdTarifa(idTarifa.intValue());
        Solicitud actualizada = solicitudRepository.save(solicitud);
        return ResponseEntity.ok(mapToDTO(actualizada));
    }

    private Integer calcularTiempoRealRuta(Solicitud solicitud) {
        Long idRuta = solicitud.getIdRuta() != null ? solicitud.getIdRuta().longValue() : null;
        if (idRuta == null) {
            return null;
        }

        List<TransporteTramoDTO> tramos = obtenerTramosYActualizarEstado(solicitud, idRuta);
        if (tramos.isEmpty()) {
            return null;
        }

        long totalMinutos = 0L;
        for (TransporteTramoDTO tramo : tramos) {
            Map<String, Object> tiempoReal = transporteClient.obtenerTiempoRealTramo(tramo.getId());
            if (tiempoReal == null || tiempoReal.isEmpty()) continue;
            Object minutosObj = tiempoReal.get("duracionMinutos");
            if (minutosObj instanceof Number numero) {
                totalMinutos += numero.longValue();
            } else if (minutosObj != null) {
                try {
                    totalMinutos += Long.parseLong(minutosObj.toString());
                } catch (NumberFormatException ignored) {}
            }
        }

        if (totalMinutos <= 0) return null;
        return (int) Math.ceil(totalMinutos / 60d);
    }

    @Transactional
    public boolean actualizarEstadoPorRuta(Integer idRuta, String nuevoEstado) {
        if (idRuta == null || nuevoEstado == null || nuevoEstado.isBlank()) {
            return false;
        }

        Solicitud solicitud = solicitudRepository.findByIdRuta(idRuta).orElse(null);
        if (solicitud == null) {
            return false;
        }

        Estado estado = buscarEstadoPorNombre(nuevoEstado, TIPO_ENTIDAD_SOLICITUD);

        if (estado == null) {
            return false;
        }

        solicitud.setEstado(estado);
        actualizarEstadoContenedor(solicitud, nuevoEstado);
        solicitudRepository.save(solicitud);
        return true;
    }

    private void actualizarEstadoContenedor(Solicitud solicitud, String estadoSolicitud) {
        if (solicitud.getContenedor() == null || estadoSolicitud == null) {
            return;
        }

        String estadoContenedorNombre = null;
        if (ESTADO_SOLICITUD_EN_TRANSITO.equalsIgnoreCase(estadoSolicitud)) {
            estadoContenedorNombre = ESTADO_CONTENEDOR_EN_TRANSITO;
        } else if (ESTADO_SOLICITUD_ENTREGADA.equalsIgnoreCase(estadoSolicitud)) {
            estadoContenedorNombre = ESTADO_CONTENEDOR_ENTREGADO;
        }

        if (estadoContenedorNombre == null) return;

        Estado estadoContenedor = buscarEstadoPorNombre(estadoContenedorNombre, TIPO_ENTIDAD_CONTENEDOR);
        if (estadoContenedor == null) return;

        boolean cambio = solicitud.getContenedor().getEstado() == null
                || !normalizar(solicitud.getContenedor().getEstado().getNombre()).equals(normalizar(estadoContenedor.getNombre()));

        if (cambio) {
            solicitud.getContenedor().setEstado(estadoContenedor);
            contenedorRepository.save(solicitud.getContenedor());
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

    private SolicitudAsignacionRutaResponseDTO crearRespuestaAsignacion(String mensaje) {
        SolicitudAsignacionRutaResponseDTO dto = new SolicitudAsignacionRutaResponseDTO();
        dto.setMensaje(mensaje);
        return dto;
    }

    private List<TransporteTramoDTO> obtenerTramosYActualizarEstado(Solicitud solicitud, Long idRuta) {
        List<TransporteTramoDTO> tramos = transporteClient.obtenerTramosPorRuta(idRuta);
        if (tramos == null) {
            tramos = Collections.emptyList();
        }
        actualizarEstadoSegunTramos(solicitud, tramos);
        return tramos;
    }

    private void actualizarEstadoSegunTramos(Solicitud solicitud, List<TransporteTramoDTO> tramos) {
        if (solicitud == null) {
            return;
        }

        String estadoObjetivo = ESTADO_SOLICITUD_PROGRAMADA;
        boolean hayTramos = tramos != null && !tramos.isEmpty();

        if (hayTramos) {
            boolean todosFinalizados = true;
            boolean algunoEnCurso = false;

            for (TransporteTramoDTO tramo : tramos) {
                boolean inicio = tramo.getFechaHoraInicio() != null;
                boolean fin = tramo.getFechaHoraFin() != null;

                if (inicio && !fin) {
                    algunoEnCurso = true;
                }

                if (!(inicio && fin)) {
                    todosFinalizados = false;
                }
            }

            if (todosFinalizados) {
                estadoObjetivo = ESTADO_SOLICITUD_ENTREGADA;
            } else if (algunoEnCurso) {
                estadoObjetivo = ESTADO_SOLICITUD_EN_TRANSITO;
            }
        }

        aplicarEstadoSolicitud(solicitud, estadoObjetivo);
    }

    private void aplicarEstadoSolicitud(Solicitud solicitud, String estadoObjetivo) {
        if (estadoObjetivo == null || estadoObjetivo.isBlank()) {
            return;
        }

        Estado estado = buscarEstadoPorNombre(estadoObjetivo, TIPO_ENTIDAD_SOLICITUD);
        if (estado == null) {
            log.warn("Estado solicitado '{}' no encontrado para tipo {}", estadoObjetivo, TIPO_ENTIDAD_SOLICITUD);
            return;
        }

        boolean cambio = solicitud.getEstado() == null
                || !normalizar(solicitud.getEstado().getNombre()).equals(normalizar(estado.getNombre()));

        if (cambio) {
            solicitud.setEstado(estado);
            actualizarEstadoContenedor(solicitud, estadoObjetivo);
        }
    }


    @SuppressWarnings("unchecked")
    private void procesarRutaTentativa(Map<String, Object> rutaTentativa, Solicitud solicitud) {
        // Retorna de inmediato si el mapa está vacío o nulo (evita null pointer)
        if (rutaTentativa == null || rutaTentativa.isEmpty()) return;

        try {
            // --- Normalización de claves ---
            // Algunas respuestas JSON vienen con ":" al final de las claves (bug de Jackson).
            // Se crea un nuevo mapa sin esos caracteres para poder acceder sin errores.
            Map<String, Object> rutaLimpia = new HashMap<>();
            for (Map.Entry<String, Object> entry : rutaTentativa.entrySet()) {
                String key = entry.getKey().replace(":", "").trim();
                rutaLimpia.put(key, entry.getValue());
            }

            // --- Caso 1: Ruta con parada intermedia (depósitos) ---
            if (rutaLimpia.containsKey("RequiereParada")) {
                Object rutasConDepositosObj = rutaLimpia.get("RutasConDepositos");

                // Se valida que efectivamente sea una lista de mapas
                if (rutasConDepositosObj instanceof List) {
                    List<Map<String, Object>> rutasConDepositos = (List<Map<String, Object>>) rutasConDepositosObj;

                    // Toma la primera ruta de la lista como la ruta “por defecto”
                    if (!rutasConDepositos.isEmpty()) {
                        Map<String, Object> primeraRuta = rutasConDepositos.get(0);

                        // Extrae costo y tiempo estimado
                        Object costo = primeraRuta.get("CostoTotalEstimado");
                        Object tiempo = primeraRuta.get("TiempoTotalHoras");

                        if (costo != null)
                            solicitud.setCostoEstimado(Double.parseDouble(costo.toString()));
                        if (tiempo != null)
                            solicitud.setTiempoEstimado(Double.parseDouble(tiempo.toString()));
                    }
                }
            } 
            // --- Caso 2: Ruta directa (sin depósito) ---
            else {
                Object costo = rutaLimpia.get("CostoTotalEstimado");
                Object tiempo = rutaLimpia.get("TiempoTotalHoras");

                if (costo != null)
                    solicitud.setCostoEstimado(Double.parseDouble(costo.toString()));
                if (tiempo != null)
                    solicitud.setTiempoEstimado(Double.parseDouble(tiempo.toString()));
            }

        } catch (Exception e) {
            System.out.println("Error procesando ruta tentativa: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public ResponseEntity<Void> eliminarSolicitud(Long id) {
        try {
            if (!solicitudRepository.existsById(id)) {
                log.warn("Intento de eliminar solicitud no existente con ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            solicitudRepository.deleteById(id);
            log.info("Solicitud eliminada exitosamente con ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error al eliminar solicitud con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private SolicitudResponseDTO mapToDTO(Solicitud solicitud) {
        SolicitudResponseDTO dto = new SolicitudResponseDTO();
        dto.setId(solicitud.getId());
        if(solicitud.getEstado() != null){
            dto.setEstado(solicitud.getEstado().getNombre());
        } else{
            dto.setEstado("Borrador");
        }
        dto.setCostoEstimado(solicitud.getCostoEstimado());
        dto.setTiempoEstimado(solicitud.getTiempoEstimado());
        dto.setCostoFinal(solicitud.getCostoFinal());
        dto.setTiempoReal(solicitud.getTiempoReal());
        dto.setIdRuta(solicitud.getIdRuta());
        dto.setIdCliente(solicitud.getIdCliente());
        dto.setIdTarifa(solicitud.getIdTarifa());
        dto.setObservaciones(solicitud.getObservaciones());
        dto.setFechaCreacion(solicitud.getFechaCreacion());

        if (solicitud.getContenedor() != null) {

            ContenedorDTO contDTO = new ContenedorDTO();
            contDTO.setId(solicitud.getContenedor().getId());
            contDTO.setPeso(solicitud.getContenedor().getPeso());
            contDTO.setAltura(solicitud.getContenedor().getAltura());
            contDTO.setAncho(solicitud.getContenedor().getAncho());
            contDTO.setLargo(solicitud.getContenedor().getLargo());
            contDTO.setVolumen(solicitud.getContenedor().getVolumen());
            if(solicitud.getContenedor().getEstado() != null) {
                contDTO.setEstado(solicitud.getContenedor().getEstado().getNombre());
            }

            dto.setContenedor(contDTO);
        }

        return dto;
    }

    


}
