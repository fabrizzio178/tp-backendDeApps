package com.tpi.ms_transporte;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MsTransportesApplication {

	public static void main(String[] args) {
		// Force canonical timezone so PostgreSQL accepts the session setting
		TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));
SpringApplication.run(MsTransportesApplication.class, args);
	}

}
