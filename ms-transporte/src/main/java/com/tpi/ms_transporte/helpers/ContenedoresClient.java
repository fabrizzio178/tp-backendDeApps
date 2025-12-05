package com.tpi.ms_transporte.helpers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ContenedoresClient {

    private final RestTemplate restTemplate;
    private final String contenedoresBaseUrl;

    public ContenedoresClient(RestTemplate restTemplate,
                              @Value("${services.contenedores.base-url:http://ms-contenedores:8081}") String contenedoresBaseUrl) {
        this.restTemplate = restTemplate;
        this.contenedoresBaseUrl = contenedoresBaseUrl;
    }

    public void actualizarEstadoPorRuta(Integer idRuta, String estado) {
        if (idRuta == null || estado == null || estado.isBlank()) return;

        String url = contenedoresBaseUrl + "/api/solicitudes/estado";
        Map<String, Object> payload = new HashMap<>();
        payload.put("idRuta", idRuta);
        payload.put("estado", estado);

        try {
            restTemplate.postForEntity(url, payload, Void.class);
        } catch (Exception ignored) {
        }
    }

    public void actualizarEstadoContenedor(Long idContenedor, String estado) {
        if (idContenedor == null || estado == null || estado.isBlank()) return;

        String url = contenedoresBaseUrl + "/api/contenedores/" + idContenedor + "/estado";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> body = new HttpEntity<>(Map.of("estado", estado), headers);

        try {
            restTemplate.put(url, body);
        } catch (Exception ignored) {
        }
    }
}
