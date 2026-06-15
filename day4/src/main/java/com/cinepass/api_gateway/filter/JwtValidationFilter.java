package com.cinepass.api_gateway.filter;

import com.cinepass.api_gateway.util.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class JwtValidationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    public JwtValidationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. Bypass authentication verification for public endpoints (such as Auth Service register/login, static uploads)
        if (path.startsWith("/auth/") || path.startsWith("/uploads/")) {
            return chain.filter(exchange);
        }

        // 2. Extract Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Unauthorized access: Missing Bearer Token in authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        // 3. Cryptographically validate the token
        if (!jwtUtil.validateToken(token)) {
            return onError(exchange, "Unauthorized access: Invalid, expired, or tampered JWT Signature", HttpStatus.UNAUTHORIZED);
        }

        // 4. Extract claims and propagate them as headers to down-stream services (Security Integration)
        try {
            String email = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);

            // Mutate request to inject headers
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Email", email)
                    .header("X-User-Role", role)
                    .build();

            System.out.println("[Gateway-Auth] Token verified. Propagating headers for user: " 
                    + email + " (Role: " + role + ")");

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            return onError(exchange, "Unauthorized access: Failed to extract JWT Claims", HttpStatus.UNAUTHORIZED);
        }
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        // Match standard API ErrorResponse structure: {"message": "...", "status": 401}
        String body = String.format("{\"message\": \"%s\", \"status\": %d}", err, status.value());
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        
        System.err.println("[Gateway-Auth-Error] Blocking request: " + err + " (" + status + ")");
        
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE; // run Jwt filter first
    }
}
