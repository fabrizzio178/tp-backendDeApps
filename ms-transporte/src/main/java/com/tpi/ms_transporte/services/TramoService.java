package com.tpi.ms_transporte.services;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tpi.ms_transporte.entities.Camion;
import com.tpi.ms_transporte.entities.Deposito;
import com.tpi.ms_transporte.entities.Estado;
import com.tpi.ms_transporte.entities.Punto;
import com.tpi.ms_transporte.entities.Ruta;
import com.tpi.ms_transporte.entities.Tramo;
import com.tpi.ms_transporte.helpers.ContenedoresClient;
import com.tpi.ms_transporte.repository.CamionRepository;
import com.tpi.ms_transporte.repository.DepositoRepository;
import com.tpi.ms_transporte.repository.EstadoRepository;
import com.tpi.ms_transporte.repository.TramoRepository;
import com.tpi.ms_transporte.services.strategy.EstrategiaRutasConDeposito;
import com.tpi.ms_transporte.services.strategy.EstrategiaRutasConocidas;
import com.tpi.ms_transporte.services.strategy.EstrategiaRutasSinDeposito;
import com.tpi.ms_transporte.services.strategy.ParametrosRutaTentativa;
import com.tpi.ms_transporte.services.strategy.interfaces.IEstrategiaRutasTentativas;

@Service
public class TramoService {

    private static final double DISTANCIA_MAX_SIN_DEPOSITO = 200.0;
    private static Logger log = LoggerFactory.getLogger(TramoService.class);

    private final TramoRepository repo;
    private final CamionRepository camionRepo;
    private final EstadoRepository estadoRepo;
    private final MapsService mapsService;
    private final ParametrosService parametrosService;
    private final DepositoRepository depositoRepo;
    private final CamionService camionService;
    private final DepositoService depositoService;
    private final RutaService rutaService;
    private final PuntoService puntoService;
    private final ContenedoresClient contenedoresClient;

    public TramoService(
            TramoRepository repo,
            CamionRepository camionRepo,
            EstadoRepository estadoRepo,
            MapsService mapsService,
            ParametrosService parametrosService,
            DepositoRepository depositoRepo,
            CamionService camionService,
            DepositoService depositoService,
            RutaService rutaService,
            PuntoService puntoService,
            ContenedoresClient contenedoresClient

    ) {
        this.repo = repo;
        this.camionRepo = camionRepo;
        this.estadoRepo = estadoRepo;
        this.mapsService = mapsService;
        this.parametrosService = parametrosService;
        this.depositoRepo = depositoRepo;
        this.camionService = camionService;
        this.depositoService = depositoService;
        this.rutaService = rutaService;
        this.puntoService = puntoService;
        this.contenedoresClient = contenedoresClient;

    }

    @Transactional(readOnly = true)
    public List<Tramo> findAll() {
        try{
            log.info("Obteniendo todos los tramos desde la base de datos.");
        } catch(Exception e){
            log.error("Error al obtener todos los tramos: {}", e.getMessage());
        }
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public Tramo findById(Long id) {
        try{
            if(id == null){
                log.warn("El ID proporcionado es nulo.");
                return null;
            }
        } catch(Exception e){
            log.error("Error al obtener tramo por ID {}: {}", id, e.getMessage());
        }
        return repo.findById(id).orElse(null);
    }

    @Transactional
    public Tramo create(Tramo tramo) {
        try{
            log.info("Creando un nuevo tramo.");
        } catch(Exception e){
            log.error("Error al crear tramo: {}", e.getMessage());
        }
        return repo.save(tramo);
    }

    @Transactional
    public Tramo update(Long id, Tramo tramo) {
        Tramo existing = repo.findById(id).orElse(null);
        if (existing == null) {
            log.error("Tramo con ID: {} no encontrado para actualizar", id);
            return null;
        }

        existing.setTipo(tramo.getTipo());
        existing.setPunto(tramo.getPunto());
        existing.setEstado(tramo.getEstado());
        existing.setCostoReal(tramo.getCostoReal());
        existing.setFechaHoraInicio(tramo.getFechaHoraInicio());
        existing.setFechaHoraFin(tramo.getFechaHoraFin());
        existing.setRuta(tramo.getRuta());
        existing.setDominioCamion(tramo.getDominioCamion());
        existing.setCostoEstimado(tramo.getCostoEstimado());
        existing.setPuntoDestino(tramo.getPuntoDestino());
        log.info("Tramo con ID: {} actualizado exitosamente", id);
        return repo.save(existing);
    }

    @Transactional
    public Tramo asignarCamion(Long idTramo, String dominioCamion) {
        log.info("Asignando camión con dominio: {} al tramo con ID: {}", dominioCamion, idTramo);
        Tramo tramo = repo.findById(idTramo).orElse(null);
        if (tramo == null){
            log.error("Tramo con ID: {} no encontrado para asignar camión", idTramo);
            return null;
        }

        if (dominioCamion != null && !dominioCamion.isBlank()) {
            if (!camionRepo.existsById(dominioCamion)) {
                log.error("Camión con dominio: {} no encontrado", dominioCamion);
                return null;
            }
            tramo.setDominioCamion(dominioCamion);
        } else {
            tramo.setDominioCamion(null);
        }
        return repo.save(tramo);
    }

    @Transactional
    public Tramo registrarInicioFin(Long idTramo, String accion) {
        log.info("Registrando {} para el tramo con ID: {}", accion, idTramo);
        Tramo tramo = repo.findById(idTramo).orElse(null);

        if (tramo == null) {
            log.error("Tramo con ID: {} no encontrado para registrar inicio/fin", idTramo);
            return null;
        }
        if (accion == null){
            log.error("Acción nula proporcionada para registrar inicio/fin del tramo con ID: {}", idTramo);
            return tramo;
        }

        String a = accion.trim().toLowerCase();
        LocalDateTime ahora = LocalDateTime.now();
        Long idRuta = tramo.getRuta() != null ? tramo.getRuta().getId() : null;

        Camion camionAsignado = null;
        if (tramo.getDominioCamion() != null) {
            camionAsignado = camionRepo.findById(tramo.getDominioCamion()).orElse(null);
        }

        if ("inicio".equals(a) || "iniciar".equals(a)) {
            boolean habiaTramoIniciado = idRuta != null && repo.findByRutaId(idRuta).stream()
                    .anyMatch(t -> t.getFechaHoraInicio() != null && !t.getId().equals(tramo.getId()));

            tramo.setFechaHoraInicio(ahora);
            Estado e = estadoRepo.findById(7L).orElse(null);
            if (e != null) tramo.setEstado(e);

            Tramo guardado = repo.save(tramo);
            if (idRuta != null && !habiaTramoIniciado) {
                contenedoresClient.actualizarEstadoPorRuta(idRuta.intValue(), "en tránsito");
                if (camionAsignado != null && camionAsignado.getIdContenedor() != null) {
                    contenedoresClient.actualizarEstadoContenedor(camionAsignado.getIdContenedor(), "en tránsito");
                }
            }
            return guardado;
        } else if ("fin".equals(a) || "final".equals(a) || "finalizar".equals(a)) {
            tramo.setFechaHoraFin(ahora);
            Estado e = estadoRepo.findById(8L).orElse(null);
            if (e != null) tramo.setEstado(e);
            if (tramo.getFechaHoraInicio() != null) {
                tramo.setCostoReal(calcularCostoEstadia(tramo));
            }
            Tramo guardado = repo.save(tramo);

            if (idRuta != null) {
                boolean todosFinalizados = repo.findByRutaId(idRuta).stream()
                        .allMatch(t -> t.getFechaHoraFin() != null);
                if (todosFinalizados) {
                    contenedoresClient.actualizarEstadoPorRuta(idRuta.intValue(), "entregada");
                }
            }
            return guardado;
        }

        return repo.save(tramo);
    }

    @Transactional
    public Map<String, Object> calcularTiempoReal(Long idTramo) {
        log.info("Calculando tiempo real para el tramo con ID: {}", idTramo);
        Tramo tramo = repo.findById(idTramo).orElse(null);
        if (tramo == null) {
            log.error("Tramo con ID: {} no encontrado para calcular tiempo real", idTramo);
            return null;
        }

        LocalDateTime inicio = tramo.getFechaHoraInicio();
        LocalDateTime fin = tramo.getFechaHoraFin();

        Map<String, Object> res = new HashMap<>();
        res.put("idTramo", tramo.getId());
        res.put("inicio", inicio);
        res.put("fin", fin);

        if (inicio == null && fin == null) {
            res.put("enCurso", false);
            res.put("duracionSegundos", 0L);
            res.put("duracionMinutos", 0L);
            res.put("duracionHoras", 0L);
            return res;
        }

        LocalDateTime hasta = fin != null ? fin : LocalDateTime.now();
        Duration d = Duration.between(inicio, hasta);

        long segundos = Math.max(0L, d.getSeconds());
        log.info("Duración calculada: {} segundos para el tramo con ID: {}", segundos, idTramo);
        res.put("enCurso", fin == null && inicio != null);
        res.put("duracionSegundos", segundos);
        res.put("duracionMinutos", segundos / 60);
        res.put("duracionHoras", segundos / 3600);

        return res;
    }

    private Deposito obtenerDepositoDelTramo(Tramo tramo) {
        if (tramo == null) {
            log.error("Tramo nulo proporcionado para obtener depósito");
            return null;
        }

        if (tramo.getPuntoDestino() != null && tramo.getPuntoDestino().getIdDeposito() != null) {
            return depositoService.findById(tramo.getPuntoDestino().getIdDeposito().longValue());
        }

        if (tramo.getPunto() != null && tramo.getPunto().getIdDeposito() != null) {
            return depositoService.findById(tramo.getPunto().getIdDeposito().longValue());
        }

        if (tramo.getRuta() != null && tramo.getRuta().getIdDeposito() != null) {
            return depositoService.findById(tramo.getRuta().getIdDeposito().longValue());
        }

        return null;
    }

    private long calcularDiasEstadia(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null) {
            return 0L;
        }

        Duration duracion = Duration.between(inicio, fin);
        long minutos = duracion.toMinutes();
        if (minutos <= 0) {
            return 0L;
        }

        double dias = minutos / (60d * 24d);
        return (long) Math.ceil(dias);
    }

    private BigDecimal calcularCostoEstadia(Tramo tramo) {
        if (tramo == null) {
            return BigDecimal.ZERO;
        }

        long diasEstadia = calcularDiasEstadia(tramo.getFechaHoraInicio(), tramo.getFechaHoraFin());
        if (diasEstadia == 0) {
            return BigDecimal.ZERO;
        }

        Deposito deposito = obtenerDepositoDelTramo(tramo);
        BigDecimal costoDiario = (deposito != null && deposito.getCostoEstadia() != null)
                ? deposito.getCostoEstadia()
                : BigDecimal.ZERO;

        return costoDiario.multiply(BigDecimal.valueOf(diasEstadia));
    }

    @Transactional
    public Map<String, Object> calcularCostoReal(Long idTramo) {
        Tramo tramo = repo.findById(idTramo).orElse(null);
        if (tramo == null) return null;

        Map<String, Object> res = new HashMap<>();
        res.put("idTramo", tramo.getId());
        res.put("inicio", tramo.getFechaHoraInicio());
        res.put("fin", tramo.getFechaHoraFin());

        Deposito deposito = obtenerDepositoDelTramo(tramo);
        BigDecimal costoDiario = (deposito != null && deposito.getCostoEstadia() != null)
                ? deposito.getCostoEstadia()
                : BigDecimal.ZERO;
        res.put("costoDiario", costoDiario);

        long diasEstadia = calcularDiasEstadia(tramo.getFechaHoraInicio(), tramo.getFechaHoraFin());
        res.put("diasEstadia", diasEstadia);

        boolean completo = tramo.getFechaHoraInicio() != null && tramo.getFechaHoraFin() != null;
        res.put("completo", completo);

        BigDecimal costoReal = completo ? calcularCostoEstadia(tramo) : BigDecimal.ZERO;
        res.put("costoReal", costoReal);

        if (completo && (tramo.getCostoReal() == null || tramo.getCostoReal().compareTo(costoReal) != 0)) {
            tramo.setCostoReal(costoReal);
            repo.save(tramo);
        }

        return res;
    }

    @Transactional(readOnly = true)
    public List<Tramo> findByRuta(Long idRuta) {
        if (idRuta == null) return Collections.emptyList();
        return repo.findByRutaId(idRuta);
    }

    public Map<String, Object> calcularRutaTentativa(Long idRuta) throws Exception {
        if (idRuta == null) {
            throw new IllegalArgumentException("El id de la ruta es obligatorio");
        }

        log.info("Calculando ruta tentativa para el idRuta: {}", idRuta);

        IEstrategiaRutasTentativas estrategia = new EstrategiaRutasConocidas(
                idRuta,
                repo,
                mapsService,
                camionService,
                depositoService,
                parametrosService
        );

        ParametrosRutaTentativa params = new ParametrosRutaTentativa(idRuta, 0d, 0d, 0d, 0d);

        Map<String, Object> resultado = estrategia.calcularRutasTentativas(params);

        try{
            resultado = persistirResultadoCalculo(resultado, 0d, 0d, 0d, 0d);
        } catch(Exception e){
            System.out.println("\nError al persistir resultado de calculo de ruta tentativa\n: " + e.getMessage());
        }

        return resultado;
    }

    public Map<String, Object> calcularRutaTentativa(double latOrigen, double lonOrigen,
                                                     double latDestino, double lonDestino) throws Exception {
        ParametrosRutaTentativa params = new ParametrosRutaTentativa(null, latOrigen, lonOrigen, latDestino, lonDestino);
        
        log.info("Calculando ruta tentativa para coordenadas: ({}, {}) a ({}, {})", latOrigen, lonOrigen, latDestino, lonDestino);

        double distancia = mapsService
                .calcularEntrePuntos(latOrigen, lonOrigen, latDestino, lonDestino)
                .getDistanciaKm();

        log.info("Distancia calculada entre puntos: {} km", distancia);

        IEstrategiaRutasTentativas estrategia = (distancia > DISTANCIA_MAX_SIN_DEPOSITO)
                ? new EstrategiaRutasConDeposito(mapsService, parametrosService, depositoRepo, camionService)
                : new EstrategiaRutasSinDeposito(mapsService, camionService, parametrosService);

        Map<String, Object> resultado = estrategia.calcularRutasTentativas(params);

        try{
            log.info("Persistiendo resultado de cálculo de ruta tentativa para coordenadas: ({}, {}) a ({}, {})", latOrigen, lonOrigen, latDestino, lonDestino);
            resultado = persistirResultadoCalculo(resultado, latOrigen, lonOrigen, latDestino, lonDestino);
        } catch(Exception e){
            log.error("Error al persistir resultado de calculo de ruta tentativa: {}", e.getMessage());
        }
        return resultado;
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public Map<String, Object> persistirResultadoCalculo(Map<String, Object> resultado,
                                        double latOrigen, double lonOrigen,
                                        double latDestino, double lonDestino) {
        Map<String, Object> enriquecido = new HashMap<>();
        if (resultado != null) {
            enriquecido.putAll(resultado);
        }

        // 1) crear la ruta y asignar idDeposito si corresponde (usa Integer)
        Ruta ruta = new Ruta();

        List<Map<String, Object>> rutasConDepositos =
                (List<Map<String, Object>>) enriquecido.get("RutasConDepositos");

        if (rutasConDepositos != null && !rutasConDepositos.isEmpty()) {
            List<String> nombres = (List<String>) rutasConDepositos.get(0).get("Depositos");
            if (nombres != null && !nombres.isEmpty()) {
                String nombrePrimero = nombres.get(0);
                List<Deposito> deps = depositoRepo.findByNombre(nombrePrimero);
                if (deps != null && !deps.isEmpty()) {
                    Deposito dep = deps.get(0);
                    ruta.setIdDeposito(dep.getId().intValue());
                }
            }
        }

        ruta = rutaService.create(ruta);
        log.info("Ruta creada con ID: {}", ruta.getId());

        // 2) casos: Sin deposito / Con deposito
        // Normalizar keys raras (por si JSON viene con ":" en nombres)
        Map<String, Object> normalized = normalizeKeys(enriquecido);
        log.info("Keys normalizadas para resultado de cálculo de ruta tentativa: {}", normalized.keySet());

        // Caso: sin depósito - asumimos que viene algo tipo "Tipo de Ruta:" o directamente RutaDirecta
        if (normalized.containsKey("Tipo de Ruta") && "Sin Deposito".equalsIgnoreCase(String.valueOf(normalized.get("Tipo de Ruta")))) {
            Tramo tramo = construirTramoDirecto(normalized, ruta, latOrigen, lonOrigen, latDestino, lonDestino);
            repo.save(tramo);
            enriquecido.put("idRuta", ruta.getId());
            return enriquecido;
        }

        // Caso: rutas con depósitos (usa "RutasConDepositos" normalizado)
        List<Map<String, Object>> rutasDepos = (List<Map<String, Object>>) normalized.get("RutasConDepositos");
        if (rutasDepos != null && !rutasDepos.isEmpty()) {
            // por simplicidad persisto sólo la primera alternativa (como venías haciendo)
            Map<String, Object> primera = rutasDepos.get(0);
            Map<String, Object> primeraNorm = normalizeKeys(primera);

            List<Map<String, Object>> detalles = (List<Map<String, Object>>) primeraNorm.get("Detalles");
            if (detalles != null) {
                // necesitamos puntos origen/destino reales para mapear cada segmento.
                Punto puntoOrigen = puntoService.findOrCreatePuntoPorCoords(latOrigen, lonOrigen, "origen");
                Punto puntoDestino = puntoService.findOrCreatePuntoPorCoords(latDestino, lonDestino, "destino");

                for (Map<String, Object> detRaw : detalles) {
                    Map<String, Object> det = normalizeKeys(detRaw);
                    Tramo t = construirTramoConDetalle(det, ruta, puntoOrigen, puntoDestino);
                    if (t.getPunto() != null && t.getPunto().getId() == null) {
                        Punto guardado = puntoService.create(t.getPunto());
                        t.setPunto(guardado);
                    }
                    if (t.getPuntoDestino() != null && t.getPuntoDestino().getId() == null) {
                        Punto guardadoDestino = puntoService.create(t.getPuntoDestino());
                        t.setPuntoDestino(guardadoDestino);
                    }
                    repo.save(t);
                }
            }
            enriquecido.put("idRuta", ruta.getId());
            return enriquecido;
        }

        // fallback: si no entendimos el JSON, retornamos ruta vacía creada.
        enriquecido.put("idRuta", ruta.getId());
        return enriquecido;
    }

    /* Normaliza keys: quita ":" y trim */
    private Map<String,Object> normalizeKeys(Map<String,Object> map) {
        Map<String,Object> out = new HashMap<>();
        if (map == null) return out;
        for (Map.Entry<String,Object> e : map.entrySet()) {
            String k = e.getKey();
            if (k == null) continue;
            String nk = k.replace(":", "").trim();
            out.put(nk, e.getValue());
        }
        return out;
    }

    /* Construir tramo directo: necesita puntos (los pasamos desde caller) */
    /* Construir tramo directo: necesita puntos (los pasamos desde caller) */
    private Tramo construirTramoDirecto(Map<String,Object> json,
                                        Ruta ruta,
                                        double latOrigen, double lonOrigen,
                                        double latDestino, double lonDestino) {

        Tramo t = new Tramo();
        t.setRuta(ruta);
        t.setTipo("origen-destino");

        // Normalizar keys
        Map<String,Object> norm = normalizeKeys(json);

        // Costo
        Object costoObj = firstOf(norm,
                "CostoTotalEstimado",
                "CostoTotalEstimado:",
                "costoTotalEstimado");

        BigDecimal costo = null;
        if (costoObj != null) {
            try { costo = new BigDecimal(costoObj.toString()); }
            catch (Exception ignored) { }
        }
        t.setCostoEstimado(costo != null ? costo : BigDecimal.ZERO);

        // Estado por defecto
        Estado e = estadoRepo.findById(6L).orElse(null);
        t.setEstado(e);

        // Puntos
        Punto origen = puntoService.findOrCreatePuntoPorCoords(latOrigen, lonOrigen, "origen");
        Punto destino = puntoService.findOrCreatePuntoPorCoords(latDestino, lonDestino, "destino");
        t.setPunto(origen);
        t.setPuntoDestino(destino);

        return t;
    }


    /* Construir tramo a partir de un detalle (Segmento, DistanciaKm, TiempoHoras, CostoEstimado)
    Necesita referencia a los puntos origen/destino generales para resolver nombres como "Origen" o "Destino". */
/* Construir tramo con detalle normalizado */
    private Tramo construirTramoConDetalle(Map<String,Object> det,
                                        Ruta ruta,
                                        Punto puntoOrigenGlobal,
                                        Punto puntoDestinoGlobal) {

        Tramo t = new Tramo();
        t.setRuta(ruta);

        // Normalizar keys
        Map<String,Object> norm = normalizeKeys(det);

        // Segmento (nombre)
        String segmento = String.valueOf(
                norm.getOrDefault("Segmento",
                norm.getOrDefault("segmento", "segmento-desconocido"))
        );

        t.setTipo(segmento);

        // Costo
        Object costoObj = firstOf(norm,
                "CostoEstimado",
                "CostoEstimado:",
                "costoEstimado");

        BigDecimal costo = null;
        if (costoObj != null) {
            try { costo = new BigDecimal(costoObj.toString()); }
            catch (Exception ignored) { }
        }
        t.setCostoEstimado(costo != null ? costo : BigDecimal.ZERO);

        // Estado inicial
        Estado e = estadoRepo.findById(6L).orElse(null);
        t.setEstado(e);

        // Resolver puntos del segmento
        String[] partes = segmento.split(" - ");
        String nombreOrigen = partes.length > 0 ? partes[0].trim() : null;
        String nombreDestino = partes.length > 1 ? partes[1].trim() : null;

        Punto origen = resolvePuntoPorNombreONombreEspecial(nombreOrigen, puntoOrigenGlobal, puntoDestinoGlobal);
        Punto destino = resolvePuntoPorNombreONombreEspecial(nombreDestino, puntoOrigenGlobal, puntoDestinoGlobal);

        t.setPunto(origen);
        t.setPuntoDestino(destino);

        return t;
    }


    /* Helper: intenta mapear "Origen"/"Destino"/"Depósito X" a Punto.
    - Si es "Origen" retorna el puntoGlobalOrigen.
    - Si es "Destino" retorna el puntoGlobalDestino.
    - Si empieza con "Depósito" busca por nombre en depositoRepo -> obtiene idDeposito,
        después busca Puntos asociados a ese deposito (supongo que tenés relación punto.idDeposito).
    - Si no lo encuentra, crea un Punto mínimo con lat/lon = null (o podés crear con coords si los tuvieras).
    */
    private Punto resolvePuntoPorNombreONombreEspecial(String nombre,
                                                    Punto puntoOrigenGlobal,
                                                    Punto puntoDestinoGlobal) {
        if (nombre == null) return null;
        String low = nombre.toLowerCase();
        if (low.contains("origen")) {
            puntoOrigenGlobal.setIdDeposito(null);
            return puntoOrigenGlobal;
        }
        if (low.contains("destino")){
            puntoDestinoGlobal.setIdDeposito(null);
            return puntoDestinoGlobal;
        }

        // buscar deposito por nombre
        Deposito dep = depositoService.findByNombre(nombre);
        if (dep != null) {
            // buscar un Punto que tenga idDeposito = dep.id
            Punto punto = puntoService.findByIdDeposito(dep.getId());
            if (punto != null) return punto;

            Punto nuevo = new Punto();
            nuevo.setTipoPunto("deposito");
            nuevo.setIdDeposito((Integer)dep.getId().intValue());
            nuevo.setLatitud((Double)dep.getLatitud().doubleValue());
            nuevo.setLongitud((Double)dep.getLongitud().doubleValue());

            return puntoService.create(nuevo);
        }

        // fallback: crear punto "placeholder" (sin coords)
        Punto fallback = new Punto();
        fallback.setTipoPunto("intermedio");
        fallback.setLatitud(null);
        fallback.setLongitud(null);
        return fallback;
    }

    /* util: devuelve el primer key existente */
    private Object firstOf(Map<String,Object> map, String... keys) {
        for (String k : keys) {
            if (map.containsKey(k) && map.get(k) != null) return map.get(k);
        }
        return null;
    }



}
