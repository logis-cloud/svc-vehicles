package com.logistica.msvehiculos.controller;

import com.logistica.msvehiculos.dto.PageResponseDTO;
import com.logistica.msvehiculos.dto.conductor.ConductorResponseDTO;
import com.logistica.msvehiculos.dto.vehiculo.CambiarEstadoDTO;
import com.logistica.msvehiculos.dto.vehiculo.VehiculoRequestDTO;
import com.logistica.msvehiculos.dto.vehiculo.VehiculoResponseDTO;
import com.logistica.msvehiculos.dto.vehiculo.VehiculoUpdateDTO;
import com.logistica.msvehiculos.entity.EstadoVehiculo;
import com.logistica.msvehiculos.entity.TipoVehiculo;
import com.logistica.msvehiculos.service.VehiculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vehiculos")
@RequiredArgsConstructor
@Tag(name = "Vehículos", description = "Gestión de la flota de vehículos")
public class VehiculoController {

    private final VehiculoService vehiculoService;

    @GetMapping
    @Operation(summary = "Lista paginada de vehículos (filtros opcionales: tipo, estado)")
    public ResponseEntity<PageResponseDTO<VehiculoResponseDTO>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) TipoVehiculo tipo,
            @RequestParam(required = false) EstadoVehiculo estado) {
        return ResponseEntity.ok(vehiculoService.listar(page, size, tipo, estado));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un vehículo, incluyendo sus conductores asignados")
    public ResponseEntity<VehiculoResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(vehiculoService.obtenerPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crea un vehículo")
    public ResponseEntity<VehiculoResponseDTO> crear(@Valid @RequestBody VehiculoRequestDTO dto) {
        VehiculoResponseDTO creado = vehiculoService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza los datos de un vehículo")
    public ResponseEntity<VehiculoResponseDTO> actualizar(@PathVariable Long id,
                                                            @Valid @RequestBody VehiculoUpdateDTO dto) {
        return ResponseEntity.ok(vehiculoService.actualizar(id, dto));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambia el estado operativo del vehículo (disponible/en_ruta/mantenimiento/inactivo)")
    public ResponseEntity<VehiculoResponseDTO> cambiarEstado(@PathVariable Long id,
                                                               @Valid @RequestBody CambiarEstadoDTO dto) {
        return ResponseEntity.ok(vehiculoService.cambiarEstado(id, dto.getEstado()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un vehículo (sus conductores quedan sin asignar, no se eliminan)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        vehiculoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/conductores")
    @Operation(summary = "Conductores actualmente asignados a un vehículo")
    public ResponseEntity<List<ConductorResponseDTO>> conductoresDelVehiculo(@PathVariable Long id) {
        return ResponseEntity.ok(vehiculoService.listarConductoresDeVehiculo(id));
    }
}
