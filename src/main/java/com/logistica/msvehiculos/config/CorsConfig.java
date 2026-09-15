package com.logistica.msvehiculos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sin esto, cualquier petición cross-origin que no sea "simple" para CORS
 * (DELETE, PUT, PATCH, o cualquier POST/PUT con Content-Type: application/json)
 * es bloqueada por el navegador ANTES de llegar al controller: el navegador
 * manda un preflight OPTIONS pidiendo permiso, Spring Boot no responde con
 * los headers Access-Control-Allow-*, y el navegador aborta la petición real.
 *
 * Por eso el frontend puede listar vehículos (GET simple) pero falla al
 * eliminar/editar (DELETE/PUT, no simples): la petición nunca llega al
 * VehiculoController ni al ConductorController.
 *
 * Equivalente Java del CORSMiddleware que ya usa el microservicio de
 * Clientes (FastAPI) en app/main.py — mismo patrón: origenes configurables
 * por variable de entorno, "*" en desarrollo por defecto.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:*}")
    private String origenesPermitidos;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origenes = origenesPermitidos.split(",");
        boolean permiteCualquiera = origenes.length == 1 && origenes[0].trim().equals("*");

        registry.addMapping("/**")
                .allowedOriginPatterns(permiteCualquiera ? new String[]{"*"} : origenes)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                // Con origen "*" no se pueden permitir credenciales (regla del spec CORS);
                // igual que en Clientes: allow_credentials=False si origenes==["*"].
                .allowCredentials(!permiteCualquiera)
                .maxAge(3600);
    }
}
