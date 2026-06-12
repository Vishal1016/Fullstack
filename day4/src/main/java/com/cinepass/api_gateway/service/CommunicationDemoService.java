package com.cinepass.api_gateway.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class CommunicationDemoService {

    private final WebClient webClient;
    private final RestTemplate restTemplate;

    public CommunicationDemoService(WebClient.Builder webClientBuilder) {
        // WebClient is non-blocking and reactive
        this.webClient = webClientBuilder.baseUrl("http://localhost:8081").build();
        // RestTemplate is blocking and synchronous
        this.restTemplate = new RestTemplate();
    }

    /**
     * Synchronous Communication Example (Blocking) using RestTemplate.
     * The thread blocks here, waiting for the external user microservice response.
     */
    public Object fetchUserProfileSync(Long userId) {
        String url = "http://localhost:8081/api/users/" + userId;
        System.out.println("[RestTemplate-Sync] Invoking service: " + url);
        return restTemplate.getForObject(url, Object.class);
    }

    /**
     * Asynchronous Communication Example (Non-blocking) using WebClient.
     * Returns a Mono (reactive wrapper); the thread registers a callback and is released immediately.
     */
    public Mono<Object> fetchUserProfileAsync(Long userId) {
        String path = "/api/users/" + userId;
        System.out.println("[WebClient-Async] Invoking reactive endpoint: http://localhost:8081" + path);
        return this.webClient.get()
                .uri(path)
                .retrieve()
                .bodyToMono(Object.class);
    }
}
