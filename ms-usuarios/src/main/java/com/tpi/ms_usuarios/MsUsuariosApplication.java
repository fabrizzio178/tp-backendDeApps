package com.tpi.ms_usuarios;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MsUsuariosApplication {
    public static void main(String[] args) {
        // Force canonical timezone so PostgreSQL accepts the session setting
        TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));
        SpringApplication.run(MsUsuariosApplication.class, args);
    }
}
