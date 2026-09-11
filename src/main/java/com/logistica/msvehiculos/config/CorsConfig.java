package com.logistica.msvehiculos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Habilita CORS para que el frontend (Amplify en producción, localhost:5173
 * en desarrollo) pueda llamar directamente a esta API desde el navegador.
 *
 * Es el equivalente Spring de la CORSMiddleware que ya usa ms-clientes
 * (FastAPI, ver app/main.py). Sin esto, el navegador bloquea las peticiones
 * fetch() del módulo de Vehículos del frontend con un error de CORS aunque
 * la API responda perfectamente bien por curl/Postman.
 *
 * CORS_ALLOWED_ORIGINS: lista separada por comas (ej. la URL de Amplify +
 * http://localhost:5173). Por defecto "*" para no bloquear el desarrollo
 * local; en producción es recomendable restringirlo al dominio real.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String origenes = System.getenv().getOrDefault("CORS_ALLOWED_ORIGINS", "*");
        String[] origenesArray = "*".equals(origenes)
                ? new String[]{"*"}
                : Arrays.stream(origenes.split(",")).map(String::trim).toArray(String[]::new);

        registry.addMapping("/**")
                .allowedOrigins(origenesArray)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}

