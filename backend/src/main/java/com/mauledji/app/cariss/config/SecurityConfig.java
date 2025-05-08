package com.mauledji.app.cariss.config;

import com.mauledji.app.cariss.security.JwtUtil;
import com.mauledji.app.cariss.security.RateLimitingFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration // Clase de configuración de seguridad
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final RateLimitingFilter rateLimitingFilter;

    // Constructor que inyecta utilidades de seguridad
    public SecurityConfig(JwtUtil jwtUtil, RateLimitingFilter rateLimitingFilter) {
        this.jwtUtil = jwtUtil;
        this.rateLimitingFilter = rateLimitingFilter;
    }

    // Bean que codifica contraseñas usando BCrypt (ideal para almacenamiento seguro)
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // 12 rondas de hash
    }

    // Configuración de CORS desde Spring Security
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:8080")); // Origen permitido
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setExposedHeaders(List.of("Authorization")); // Permitir lectura de este header
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Duración del preflight cache

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Aplica a todas las rutas
        return source;
    }

    // Filtro principal de seguridad HTTP
    @Bean
    @Order(1) // Prioridad del filtro de seguridad
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Configuración de CSRF (usamos cookies para manejarlo)
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/api/auth/**") // Ignora CSRF para endpoints de autenticación
            )
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Aplica la config CORS anterior

            // Configuración de cabeceras de seguridad HTTP
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                .xssProtection(Customizer.withDefaults())
                .contentSecurityPolicy(csp -> 
                    csp.policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline';"))
            )

            // Configura rutas públicas y privadas
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Permitir preflight OPTIONS
                .requestMatchers("/", "/index.html", "/register.html", "/home.html", "/favicon.ico",
                                 "/api/auth/**", "/css/**", "/js/**", "/images/**").permitAll() // rutas públicas
                .anyRequest().authenticated() // todo lo demás requiere autenticación
            )

            // Filtro personalizado para limitar peticiones
            .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)

            // Filtro personalizado para validar JWT en cada request
            .addFilterBefore((request, response, chain) -> {
                HttpServletRequest req = (HttpServletRequest) request;
                HttpServletResponse res = (HttpServletResponse) response;

                // Headers de seguridad recomendados por OWASP
                res.setHeader("X-Content-Type-Options", "nosniff");
                res.setHeader("X-Frame-Options", "DENY");
                res.setHeader("X-XSS-Protection", "1; mode=block");
                res.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

                String path = req.getRequestURI();
                String authHeader = req.getHeader(HttpHeaders.AUTHORIZATION);

                // Ignora filtros para rutas públicas
                if (path.startsWith("/api/auth") || path.startsWith("/css") || 
                    path.startsWith("/js") || path.equals("/favicon.ico")) {
                    chain.doFilter(request, response);
                    return;
                }

                // Si el token es válido, se establece el usuario en el contexto de seguridad
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    if (jwtUtil.validateToken(token)) {
                        String username = jwtUtil.extractUsername(token);
                        UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, null);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } else {
                        res.setStatus(HttpStatus.UNAUTHORIZED.value());
                        return;
                    }
                }

                // Continúa con la cadena de filtros
                chain.doFilter(request, response);
            }, UsernamePasswordAuthenticationFilter.class);

        return http.build(); // Construye la configuración final
    }
}
