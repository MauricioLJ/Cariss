package com.mauledji.app.cariss.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component // Anota esta clase como componente para que Spring la detecte y registre automáticamente como filtro
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 5; // Máximo de solicitudes permitidas
    private static final int WINDOW_MINUTES = 1; // Tiempo en minutos para contar las solicitudes

    // Mapa que lleva la cuenta de solicitudes por IP
    private final Map<String, RequestTracker> requestCounts = new ConcurrentHashMap<>();

    // Este método se ejecuta para cada solicitud HTTP
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Aplicamos el filtro solo a las rutas de autenticación
        if (request.getRequestURI().startsWith("/api/auth/")) {

            String clientIp = getClientIp(request); // Obtener IP del cliente
            RequestTracker tracker = requestCounts.computeIfAbsent(clientIp, k -> new RequestTracker()); // Obtener o crear el contador

            if (tracker.isLimited()) {
                // Si se excede el límite, respondemos con 429 Too Many Requests
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Too many requests. Please try again later.");
                return;
            }

            tracker.incrementCount(); // Incrementar contador si aún no se ha limitado
        }

        // Continuar con el resto de los filtros si no se ha bloqueado
        filterChain.doFilter(request, response);
    }

    // Método para obtener la IP del cliente, considerando el header "X-Forwarded-For" si está presente
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim(); // Usamos solo la primera IP
        }
        return request.getRemoteAddr(); // Si no existe el header, usar IP remota directa
    }

    // Clase interna que lleva el conteo de solicitudes y la ventana de tiempo
    private static class RequestTracker {
        private final AtomicInteger count = new AtomicInteger(0); // Contador de solicitudes
        private long windowStart = System.currentTimeMillis(); // Inicio de la ventana de tiempo

        // Determina si se ha excedido el número de solicitudes permitidas
        synchronized boolean isLimited() {
            long now = System.currentTimeMillis();
            if (now - windowStart > WINDOW_MINUTES * 60 * 1000) {
                // Si la ventana de tiempo terminó, reiniciamos el contador
                count.set(0);
                windowStart = now;
                return false;
            }
            // Comprobamos si el número de solicitudes supera el máximo permitido
            return count.get() >= MAX_REQUESTS;
        }

        // Incrementa el número de solicitudes
        void incrementCount() {
            count.incrementAndGet();
        }
    }
}
