package com.tpi.gateway.config;

import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class CorrelationIdConfig {

    private static final String HEADER = "X-Correlation-ID";

    @Bean
    public GlobalFilter correlationIdFilter() {

        return (exchange, chain) -> {

            String headerValue = exchange.getRequest().getHeaders().getFirst(HEADER);
            final String correlationId = (headerValue == null || headerValue.isBlank())
                    ? UUID.randomUUID().toString()
                    : headerValue;

            // Logback MDC
            MDC.put("correlationId", correlationId);

            // Mutar request para que el header viaje a los MS
            var mutatedRequest = exchange.getRequest()
                    .mutate()
                    .header(HEADER, correlationId)
                    .build();

            var mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();

            return chain.filter(mutatedExchange)
                    .then(
                        Mono.fromRunnable(() -> {
                            // Setear el header en la response
                            exchange.getResponse().getHeaders().set(HEADER, correlationId);

                            // Limpiar MDC
                            MDC.remove("correlationId");
                        })
                    );
        };
    }
}
