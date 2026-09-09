package com.logistica.msvehiculos.controller;

import com.logistica.msvehiculos.dto.PageResponseDTO;
import com.logistica.msvehiculos.dto.conductor.ConductorRequestDTO;
import com.logistica.msvehiculos.dto.conductor.ConductorResponseDTO;
import com.logistica.msvehiculos.dto.conductor.ConductorUpdateDTO;
import com.logistica.msvehiculos.entity.Turno;
import com.logistica.msvehiculos.service.ConductorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/conductores")
@RequiredArgsConstructor
@Tag(name = "Conductores", description = "Gestión de conductores y su asignación a vehículos")
public class ConductorController {

    private final ConductorService conductorService;

    @GetMapping
    @Operation(summary = "Lista paginada de conductores (filtros opcionales: turno, idVehiculo, activo)")
    public ResponseEntity<PageResponseDTO<ConductorResponseDTO>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Turno turno,
            @RequestParam(required = false) Long idVehiculo,
            @RequestParam(required = false) Boolean activo) {
        return ResponseEntity.ok(conductorService.listar(page, size, turno, idVehiculo, activo));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un conductor")
    public ResponseEntity<ConductorResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(conductorService.obtenerPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crea un conductor (opcionalmente con vehículo asignado)")
    public ResponseEntity<ConductorResponseDTO> crear(@Valid @RequestBody ConductorRequestDTO dto) {
        ConductorResponseDTO creado = conductorService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza los datos de un conductor")
    public ResponseEntity<ConductorResponseDTO> actualizar(@PathVariable Long id,
                                                             @Valid @RequestBody ConductorUpdateDTO dto) {
        return ResponseEntity.ok(conductorService.actualizar(id, dto));
    }

    @PatchMapping("/{id}/asignar-vehiculo/{idVehiculo}")
    @Operation(summary = "Asigna o reasigna un vehículo al conductor (rotación por turno)")
    public ResponseEntity<ConductorResponseDTO> asignarVehiculo(@PathVariable Long id, @PathVariable Long idVehiculo) {
        return ResponseEntity.ok(conductorService.asignarVehiculo(id, idVehiculo));
    }

    @PatchMapping("/{id}/liberar")
    @Operation(summary = "Libera al conductor de su vehículo actual (queda sin asignar)")
    public ResponseEntity<ConductorResponseDTO> liberar(@PathVariable Long id) {
        return ResponseEntity.ok(conductorService.liberarVehiculo(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un conductor")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        conductorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
