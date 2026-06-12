package com.cinepass.api_gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        System.out.println("[Gateway-Router] ---> Incoming Request: " 
                + request.getMethod() + " " + request.getURI());

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            System.out.println("[Gateway-Router] <--- Outgoing Response: " 
                    + exchange.getResponse().getStatusCode() + " " + request.getURI());
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
