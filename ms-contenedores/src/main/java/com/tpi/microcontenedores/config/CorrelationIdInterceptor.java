package com.tpi.microcontenedores.config;

import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class CorrelationIdInterceptor implements ClientHttpRequestInterceptor {

    private static final String HEADER = "X-Correlation-ID";
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CorrelationIdInterceptor.class);

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {

        String correlationId = MDC.get("correlationId");

        log.info("[Interceptor] Enviando request con Correlation ID: {}", correlationId);

        if (correlationId != null) {
            request.getHeaders().set(HEADER, correlationId);
        }

        return execution.execute(request, body);
    }
}
