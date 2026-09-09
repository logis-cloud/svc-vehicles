package com.logistica.msvehiculos.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Endpoint informativo en la raíz, útil al entrar desde el navegador
 * (evita depender del comportamiento por defecto de recursos estáticos).
 */
@RestController
public class RootController {

    @GetMapping("/")
    public Map<String, String> info() {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("servicio", "ms-vehiculos");
        body.put("descripcion", "Microservicio de Vehículos y Flota - Sistema de Logística y Entregas");
        body.put("swagger", "/docs");
        body.put("openapi", "/v3/api-docs");
        body.put("health", "/actuator/health");
        return body;
    }
}
