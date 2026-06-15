package com.cinepass.movie_service.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SecurityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod();
        String path = request.getRequestURI();

        // Bypass security interceptor for error routing and static resources (like uploads)
        if (path.startsWith("/error") || path.startsWith("/uploads/")) {
            return true;
        }

        // Get security headers from API Gateway
        String userEmail = request.getHeader("X-User-Email");
        String userRole = request.getHeader("X-User-Role");

        // Mutating operations on Movies or Uploads require Admin or Theatre Owner privileges
        if (path.startsWith("/api/movies") || path.startsWith("/movies")) {
            if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
                if (userRole == null) {
                    sendErrorResponse(response, "Unauthorized: Missing authentication headers from Gateway", HttpStatus.UNAUTHORIZED);
                    return false;
                }
                
                String roleLower = userRole.toLowerCase();
                if (!roleLower.contains("admin") && !roleLower.contains("owner") && !roleLower.contains("theatre_owner")) {
                    sendErrorResponse(response, "Forbidden: Only Administrators or Theatre Owners can manage movies", HttpStatus.FORBIDDEN);
                    return false;
                }
            }
        }

        // Creating or modifying reviews requires a logged-in user role
        if (path.startsWith("/api/reviews") || path.startsWith("/reviews")) {
            if ("POST".equalsIgnoreCase(method)) {
                if (userRole == null) {
                    sendErrorResponse(response, "Unauthorized: Missing authentication headers from Gateway", HttpStatus.UNAUTHORIZED);
                    return false;
                }
            }
        }

        return true;
    }

    private void sendErrorResponse(HttpServletResponse response, String message, HttpStatus status) throws Exception {
        response.setStatus(status.value());
        response.setContentType("application/json");
        String jsonPayload = String.format("{\"status\":\"error\",\"message\":\"%s\",\"data\":null}", message);
        response.getWriter().write(jsonPayload);
    }
}
