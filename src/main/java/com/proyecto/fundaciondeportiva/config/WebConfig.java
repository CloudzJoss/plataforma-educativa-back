package com.proyecto.fundaciondeportiva.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**") // Aplica CORS a todas las rutas bajo /api
                        .allowedOrigins( // Orígenes permitidos (direcciones del frontend)
                                "http://localhost:3000", // React por defecto
                                "http://127.0.0.1:5500"  // Live Server de VS Code (HTML simple)
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Métodos HTTP permitidos
                        .allowedHeaders("*") // Cabeceras permitidas (como Authorization)
                        .allowCredentials(true); // Permite enviar cookies/credenciales (útil si usas sesiones, aunque JWT no las necesita tanto)
            }
        };
    }
}