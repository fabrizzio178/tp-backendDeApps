package com.tpi.gateway.config;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] OPERADOR_ADMIN = { "OPERADOR", "ADMIN" };
    private static final String[] CLIENTE_OPERADOR_ADMIN = { "CLIENTE", "OPERADOR", "ADMIN" };
    private static final String[] TRANSPORTISTA_ADMIN = { "TRANSPORTISTA", "ADMIN" };
    private static final String[] CLIENTE_ADMIN = { "CLIENTE", "ADMIN" };

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchange -> exchange
                // Public endpoints exposed by the gateway
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/usuarios/public/**", "/contenedores/public/**", "/tarifas/public/**").permitAll()

                // Usuarios microservice
                .pathMatchers("/usuarios/**").hasAnyRole(OPERADOR_ADMIN)
                .pathMatchers("/clientes/**", "/transportistas/**").hasAnyRole(OPERADOR_ADMIN)

                // Contenedores microservice
                .pathMatchers(HttpMethod.POST, "/solicitudes", "/solicitudes/").permitAll()
                .pathMatchers(HttpMethod.POST, "/solicitudes/*/ruta/*", "/solicitudes/*/ruta/**").hasAnyRole(OPERADOR_ADMIN)
                .pathMatchers("/contenedores").hasAnyRole(OPERADOR_ADMIN)
                .pathMatchers("/solicitudes").hasAnyRole(CLIENTE_OPERADOR_ADMIN)
                .pathMatchers("/solicitudes/**").hasAnyRole(CLIENTE_OPERADOR_ADMIN)
                .pathMatchers("/contenedores/*/estado").hasAnyRole(CLIENTE_OPERADOR_ADMIN)
                .pathMatchers("/contenedores/**").hasAnyRole(OPERADOR_ADMIN)

                // Transporte microservice
                .pathMatchers("/camiones").hasAnyRole(OPERADOR_ADMIN)
                .pathMatchers("/depositos").hasAnyRole(OPERADOR_ADMIN)
                .pathMatchers("/puntos").hasAnyRole(OPERADOR_ADMIN)
                .pathMatchers("/rutas").hasAnyRole(OPERADOR_ADMIN)
                .pathMatchers("/tramos").hasAnyRole(OPERADOR_ADMIN)
                .pathMatchers("/tramos/*/inicio", "/tramos/*/fin").hasAnyRole(TRANSPORTISTA_ADMIN)
                .pathMatchers("/tramos/*/camion").hasAnyRole(OPERADOR_ADMIN)
                .pathMatchers("/tramos", "/tramos/**").hasAnyRole(OPERADOR_ADMIN)
                .pathMatchers("/camiones/*/validar").hasAnyRole(CLIENTE_OPERADOR_ADMIN)
                .pathMatchers("/camiones/**").hasAnyRole(OPERADOR_ADMIN)
                .pathMatchers("/depositos/**").hasAnyRole(OPERADOR_ADMIN)
                .pathMatchers("/puntos/**").hasAnyRole(OPERADOR_ADMIN)
                .pathMatchers("/rutas/calcular").hasAnyRole(CLIENTE_ADMIN)
                .pathMatchers("/rutas/**").hasAnyRole(OPERADOR_ADMIN)

                // Tarifas microservice
                .pathMatchers("/tarifas").hasAnyRole(OPERADOR_ADMIN)
                .pathMatchers("/tarifas/costo").hasAnyRole(CLIENTE_ADMIN)
                .pathMatchers("/tarifas/**").hasAnyRole(OPERADOR_ADMIN)

                //Clientes microservice
                .pathMatchers("/clientes").hasAnyRole(OPERADOR_ADMIN)
                .pathMatchers("/transportistas").hasAnyRole(OPERADOR_ADMIN)
                // Everything else requires authentication
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(grantedAuthoritiesConverter()))
            )
            .build();
    }

    private ReactiveJwtAuthenticationConverterAdapter grantedAuthoritiesConverter() {
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Set<String> roles = new HashSet<>();

            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null) {
                Object realmRoles = realmAccess.get("roles");
                if (realmRoles instanceof Collection<?> collection) {
                    collection.stream()
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .forEach(roles::add);
                }
            }

            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess != null) {
                resourceAccess.values().forEach(entry -> {
                    if (entry instanceof Map<?, ?> resource) {
                        Object clientRoles = resource.get("roles");
                        if (clientRoles instanceof Collection<?> collection) {
                            collection.stream()
                                .filter(Objects::nonNull)
                                .map(Object::toString)
                                .forEach(roles::add);
                        }
                    }
                });
            }

            return roles.stream()
                .map(role -> "ROLE_" + role.toUpperCase(Locale.ROOT))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
        });

        return new ReactiveJwtAuthenticationConverterAdapter(jwtConverter);
    }
}
