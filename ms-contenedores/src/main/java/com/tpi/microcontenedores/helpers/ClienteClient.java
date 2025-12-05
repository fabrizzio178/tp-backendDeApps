package com.tpi.microcontenedores.helpers;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.tpi.microcontenedores.dto.ClienteDTO;

@Component
public class ClienteClient {

    private static final Logger log = LoggerFactory.getLogger(ClienteClient.class);
    private static final String CLIENTES_BASE_URL = "http://ms-usuarios:8083/api/clientes";

    private final RestTemplate restTemplate;

    public ClienteClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Optional<ClienteDTO> obtenerClientePorId(Long id, String authHeader) {
        if (id == null) {
            return Optional.empty();
        }

        try {
            HttpEntity<Void> entity = new HttpEntity<>(buildHeaders(authHeader));
            ResponseEntity<ClienteDTO> response = restTemplate.exchange(
                CLIENTES_BASE_URL + "/" + id,
                HttpMethod.GET,
                entity,
                ClienteDTO.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return Optional.ofNullable(response.getBody());
            }
            return Optional.empty();
        } catch (HttpClientErrorException.NotFound ex) {
            return Optional.empty();
        } catch (RestClientException ex) {
            log.error("Error consultando cliente {}: {}", id, ex.getMessage());
            throw ex;
        }
    }

    public ClienteDTO crearCliente(ClienteDTO cliente, String authHeader) {
        if (cliente == null) {
            return null;
        }

        ClienteDTO body = new ClienteDTO(
            null,
            cliente.getNombre(),
            cliente.getApellido(),
            cliente.getDni(),
            cliente.getMail(),
            cliente.getNumero()
        );

        HttpHeaders headers = buildHeaders(authHeader);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ClienteDTO> entity = new HttpEntity<>(body, headers);
        ResponseEntity<ClienteDTO> response = restTemplate.postForEntity(
            CLIENTES_BASE_URL,
            entity,
            ClienteDTO.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        }
        return null;
    }

    private HttpHeaders buildHeaders(String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        if (authHeader != null && !authHeader.isBlank()) {
            headers.set("Authorization", authHeader);
        }
        return headers;
    }
}
