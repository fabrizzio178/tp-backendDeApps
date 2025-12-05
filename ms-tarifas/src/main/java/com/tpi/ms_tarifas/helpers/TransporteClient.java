package com.tpi.ms_tarifas.helpers;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.tpi.ms_tarifas.dto.transporte.DepositoDTO;
import com.tpi.ms_tarifas.dto.transporte.RutaDTO;
import com.tpi.ms_tarifas.dto.transporte.TramoDTO;

import java.util.List;

@Component
public class TransporteClient {

    private final RestTemplate restTemplate;
    private final String baseUrl = "http://ms-transporte:8082/api";

    public TransporteClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public RutaDTO obtenerRuta(Long idRuta, String authHeader) {
        String url = baseUrl + "/rutas/" + idRuta;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(url, HttpMethod.GET, entity, RutaDTO.class).getBody();
    }

    public List<TramoDTO> obtenerTramos(Long idRuta, String authHeader) {

        String url = baseUrl + "/tramos/ruta/" + idRuta;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List<TramoDTO>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            new org.springframework.core.ParameterizedTypeReference<List<TramoDTO>>() {}
        );

        return response.getBody();
    }

    public DepositoDTO obtenerDeposito(Long idDeposito, String authHeader) {
    String url = baseUrl + "/depositos/" + idDeposito;

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", authHeader);

    HttpEntity<Void> entity = new HttpEntity<>(headers);

    return restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            DepositoDTO.class
    ).getBody();
    }
}