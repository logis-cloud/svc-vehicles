package com.logistica.msvehiculos.controller;

import com.logistica.msvehiculos.seeder.DataSeeder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Solo se registra si app.seed.enabled=true (variable de entorno SEED_ENABLED).
 * Pensado únicamente para entornos de desarrollo/pruebas y para la carga
 * única de datos ficticios pedida por la rúbrica.
 *
 * Defaults alineados al requisito de la rúbrica: "insertar masivamente, por
 * única vez, datos ficticios en al menos 1 tabla de cada base de datos SQL
 * (mínimo 20,000 registros)". En esta base de datos PostgreSQL la tabla que
 * cumple ese mínimo es "conductores" (20,000 por defecto); "vehiculos" se
 * mantiene en un volumen más realista para una flota (4,000 por defecto).
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Utilidades de desarrollo, habilitadas solo si SEED_ENABLED=true")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class AdminController {

    private final DataSeeder dataSeeder;

    @PostMapping("/seed")
    @Operation(summary = "Carga datos ficticios de vehículos y conductores (solo desarrollo). "
            + "Debe ejecutarse una única vez: no valida duplicados contra datos ya existentes.")
    public ResponseEntity<Map<String, Integer>> seed(
            @RequestParam(defaultValue = "4000") int vehiculos,
            @RequestParam(defaultValue = "20000") int conductores) {
        return ResponseEntity.ok(dataSeeder.seed(vehiculos, conductores));
    }
}
