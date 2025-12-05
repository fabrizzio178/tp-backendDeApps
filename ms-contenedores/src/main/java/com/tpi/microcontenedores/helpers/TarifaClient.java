package com.tpi.microcontenedores.helpers;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TarifaClient {

    private static final String TARIFAS_BASE_URL = "http://ms-tarifas:8084/api/tarifas";

    private final RestTemplate restTemplate;

    public TarifaClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("unchecked")
    public BigDecimal obtenerCostoRealRuta(Long idRuta, String token) {
        if (idRuta == null) return null;

        String url = TARIFAS_BASE_URL + "/ruta/" + idRuta + "/costos";
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        if (response == null) return null;

        Object costoReal = response.get("costoRealTotal");
        if (costoReal == null) return null;

        try {
            return new BigDecimal(costoReal.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
