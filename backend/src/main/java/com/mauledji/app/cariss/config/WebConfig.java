package com.mauledji.app.cariss.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // Indica que esta clase es una clase de configuración de Spring.
public class WebConfig {

    // Define un bean que configura CORS de manera global para controladores que no usan Spring Security.
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Aplica CORS a todas las rutas (/**)
                registry.addMapping("/**")
                        .allowedOrigins("*") // Permite solicitudes desde cualquier origen
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Métodos permitidos
                        .allowedHeaders("*"); // Todos los headers permitidos
            }
        };
    }
}
