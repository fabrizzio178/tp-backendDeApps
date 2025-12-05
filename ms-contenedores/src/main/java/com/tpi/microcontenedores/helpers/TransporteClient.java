package com.tpi.microcontenedores.helpers;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.tpi.microcontenedores.dto.CoordenadasDTO;
import com.tpi.microcontenedores.dto.TransporteRutaDTO;
import com.tpi.microcontenedores.dto.TransporteTramoDTO;


@Component
public class TransporteClient {

    private final RestTemplate restTemplate;
    private static final String RUTAS_BASE_URL = "http://ms-transporte:8082/api/rutas";
    private static final String TRAMOS_BASE_URL = "http://ms-transporte:8082/api/tramos";

    public TransporteClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> calcularRutasTentativas(CoordenadasDTO dto, String authHeader) {
        String url = RUTAS_BASE_URL + "/calcular";

        HttpHeaders headers = buildHeaders(authHeader);
        headers.set("Content-Type", "application/json");

        HttpEntity<CoordenadasDTO> entity = new HttpEntity<>(dto, headers);

        return restTemplate.postForObject(url, entity, Map.class);
    }

    public Map<String, Object> obtenerRutaTentativa(Long idRuta, String authHeader) {

        String url = RUTAS_BASE_URL + "/ruta-tentativa/" + idRuta;

        HttpHeaders headers = buildHeaders(authHeader);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode().is4xxClientError()) {
                return Collections.emptyMap();
            }
            throw ex;
        }
    }

    public List<TransporteTramoDTO> obtenerTramosPorRuta(Long idRuta) {
        if (idRuta == null) return Collections.emptyList();

        String url = TRAMOS_BASE_URL + "/ruta/" + idRuta;
        try {
            var response = restTemplate.getForEntity(url, TransporteTramoDTO[].class);
            TransporteTramoDTO[] body = response.getBody();
            if (body == null) return Collections.emptyList();
            return List.of(body);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> obtenerTiempoRealTramo(Long idTramo) {
        if (idTramo == null) return Collections.emptyMap();
        String url = TRAMOS_BASE_URL + "/" + idTramo + "/tiempo-real";
        try {
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    public Long obtenerUltimaRutaId() {
        try {
            var response = restTemplate.getForEntity(RUTAS_BASE_URL, TransporteRutaDTO[].class);
            TransporteRutaDTO[] rutas = response.getBody();
            if (rutas == null || rutas.length == 0) return null;
            Long max = null;
            for (TransporteRutaDTO ruta : rutas) {
                if (ruta.getId() == null) continue;
                if (max == null || ruta.getId() > max) {
                    max = ruta.getId();
                }
            }
            return max;
        } catch (Exception e) {
            return null;
        }
    }

    private HttpHeaders buildHeaders(String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        if (authHeader != null && !authHeader.isBlank()) {
            headers.set("Authorization", authHeader);
        }
        return headers;
    }
}
