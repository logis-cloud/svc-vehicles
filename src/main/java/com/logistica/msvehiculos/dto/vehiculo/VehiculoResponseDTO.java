package com.logistica.msvehiculos.dto.vehiculo;

import com.logistica.msvehiculos.dto.conductor.ConductorResumenDTO;
import com.logistica.msvehiculos.entity.EstadoVehiculo;
import com.logistica.msvehiculos.entity.TipoVehiculo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehiculoResponseDTO {

    private Long id;
    private String placa;
    private TipoVehiculo tipo;
    private BigDecimal capacidadKg;
    private String marca;
    private String modelo;
    private Integer anioFabricacion;
    private EstadoVehiculo estado;
    private LocalDateTime fechaRegistro;

    /** Solo se llena en el detalle (GET /vehiculos/{id}); en listados va null. */
    private List<ConductorResumenDTO> conductores;
}
