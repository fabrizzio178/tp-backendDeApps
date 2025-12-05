package com.tpi.ms_tarifas.controller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/tarifas")
public class TestAuthController {

    private final Logger log = LoggerFactory.getLogger(TestAuthController.class);

    @GetMapping("/public/hello")
    public String publico() {
        log.info("Acceso a endpoint público");
        return "Endpoint público de tarifas 👋";
    }

    @GetMapping("/me")
    public Map<String, String> usuarioActual() {
        log.info("Consulta de información del usuario actual");
        return Map.of("message", "Autenticación gestionada por el gateway");
    }

    @GetMapping("/admin/only")
    public String soloAdmin() {
        log.info("Acceso a endpoint solo para ADMIN");
        return "Solo ADMIN puede ver esto ✅";
    }
}
