package com.tpi.microcontenedores.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/contenedores")
public class TestAuthController {

    @GetMapping("/public/hello")
    public String publico() {
        return "Endpoint público de contenedores 👋";
    }

    @GetMapping("/me")
    public Map<String, String> usuarioActual() {
        return Map.of("message", "Autenticación gestionada por el gateway");
    }

    @GetMapping("/admin/only")
    public String soloAdmin() {
        return "Solo ADMIN puede ver esto ✅";
    }
}
