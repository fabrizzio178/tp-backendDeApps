package com.tpi.ms_transporte.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpi.ms_transporte.dto.DistanciaDTO;


@Service
public class MapsService {

    @Value("${google.maps.apikey}")
    private String apiKey;

    private final RestClient.Builder builder;
    private static final Logger log = LoggerFactory.getLogger(MapsService.class);

    public MapsService(RestClient.Builder builder) {
        this.builder = builder;
    }

    public DistanciaDTO calcularEntrePuntos(double lat1, double lon1, double lat2, double lon2) throws Exception {
        String origen = lat1 + "," + lon1;
        String destino = lat2 + "," + lon2;

        RestClient client = builder
            .baseUrl("https://maps.googleapis.com/maps/api")
            .build();
        
        log.info("Calculando distancia entre puntos: {} y {}", origen, destino);

        String url = "/distancematrix/json?origins=" + origen +
                    "&destinations=" + destino +
                    "&units=metric&key=" + apiKey;

        ResponseEntity<String> response =
            client.get()
                .uri(url)
                .retrieve()
                .toEntity(String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());

        // validar status general
        String status = root.path("status").asText(null);
        if (status == null || !"OK".equalsIgnoreCase(status)) {
            log.error("Maps API returned non-OK status: {}", status);
            throw new RuntimeException("Maps API status: " + status);
        }

        JsonNode element = root.path("rows").get(0).path("elements").get(0);
        String elemStatus = element.path("status").asText(null);
        if (elemStatus == null || !"OK".equalsIgnoreCase(elemStatus)) {
            log.error("Maps element returned non-OK status: {}", elemStatus);
            throw new RuntimeException("Maps element status: " + elemStatus);
        }

        JsonNode distanceNode = element.path("distance");
        JsonNode durationNode = element.path("duration");
        if (distanceNode.isMissingNode() || durationNode.isMissingNode()) {
            log.error("Maps response missing distance or duration information.");
            throw new RuntimeException("Maps response missing distance/duration");
        }

        DistanciaDTO dto = new DistanciaDTO();
        dto.setPuntoOrigen(origen);
        dto.setPuntoDestino(destino);
        dto.setDistanciaKm(distanceNode.path("value").asDouble() / 1000.0);
        dto.setDuracionTexto(durationNode.path("text").asText());
        long minutos = parsearMinutos(dto.getDuracionTexto());
        dto.setDuracionHoras(minutos / 60.0);
        log.info("Distancia calculada: {} km, Duración: {} horas", dto.getDistanciaKm(), dto.getDuracionHoras());
        return dto;
    }


    private long parsearMinutos(String texto) {
        if (texto == null || texto.isBlank()) {
            return 0;
        }

        String normalized = texto
                .toLowerCase()
                .replaceAll(",", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (normalized.isEmpty()) {
            return 0;
        }

        long total = 0;
        String[] tokens = normalized.split(" ");

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            long value;

            try {
                value = Long.parseLong(token);
            } catch (NumberFormatException ex) {
                continue;
            }

            if (i + 1 >= tokens.length) {
                continue;
            }

            String unit = tokens[i + 1].replaceAll("[^a-z]", "");

            if (unit.startsWith("day")) {
                total += value * 24 * 60;
                i++;
            } else if (unit.startsWith("hour") || unit.startsWith("hr")) {
                total += value * 60;
                i++;
            } else if (unit.startsWith("min")) {
                total += value;
                i++;
            } else if (unit.startsWith("sec")) {
                // seconds rarely appear; convert to minutes fractionally
                total += value / 60;
                i++;
            }
        }

        return total;
    }

}

