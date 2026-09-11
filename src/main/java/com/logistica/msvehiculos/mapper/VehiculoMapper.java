package com.logistica.msvehiculos.mapper;

import com.logistica.msvehiculos.dto.conductor.ConductorResumenDTO;
import com.logistica.msvehiculos.dto.vehiculo.VehiculoRequestDTO;
import com.logistica.msvehiculos.dto.vehiculo.VehiculoResponseDTO;
import com.logistica.msvehiculos.dto.vehiculo.VehiculoResumenDTO;
import com.logistica.msvehiculos.dto.vehiculo.VehiculoUpdateDTO;
import com.logistica.msvehiculos.entity.Conductor;
import com.logistica.msvehiculos.entity.Vehiculo;

import java.util.List;
import java.util.stream.Collectors;

public final class VehiculoMapper {

    private VehiculoMapper() {
    }

    public static Vehiculo toEntity(VehiculoRequestDTO dto) {
        return Vehiculo.builder()
                .placa(dto.getPlaca().toUpperCase())
                .tipo(dto.getTipo())
                .capacidadKg(dto.getCapacidadKg())
                .marca(dto.getMarca())
                .modelo(dto.getModelo())
                .anioFabricacion(dto.getAnioFabricacion())
                .build();
    }

    public static void actualizarEntity(Vehiculo vehiculo, VehiculoUpdateDTO dto) {
        vehiculo.setPlaca(dto.getPlaca().toUpperCase());
        vehiculo.setTipo(dto.getTipo());
        vehiculo.setCapacidadKg(dto.getCapacidadKg());
        vehiculo.setMarca(dto.getMarca());
        vehiculo.setModelo(dto.getModelo());
        vehiculo.setAnioFabricacion(dto.getAnioFabricacion());
    }

    public static VehiculoResponseDTO toResponseDTO(Vehiculo vehiculo, boolean incluirConductores) {
        VehiculoResponseDTO.VehiculoResponseDTOBuilder builder = VehiculoResponseDTO.builder()
                .id(vehiculo.getId())
                .placa(vehiculo.getPlaca())
                .tipo(vehiculo.getTipo())
                .capacidadKg(vehiculo.getCapacidadKg())
                .marca(vehiculo.getMarca())
                .modelo(vehiculo.getModelo())
                .anioFabricacion(vehiculo.getAnioFabricacion())
                .estado(vehiculo.getEstado())
                .fechaRegistro(vehiculo.getFechaRegistro());

        if (incluirConductores && vehiculo.getConductores() != null) {
            List<ConductorResumenDTO> resumenes = vehiculo.getConductores().stream()
                    .map(VehiculoMapper::toConductorResumenDTO)
                    .collect(Collectors.toList());
            builder.conductores(resumenes);
        }
        return builder.build();
    }

    public static VehiculoResumenDTO toResumenDTO(Vehiculo vehiculo) {
        if (vehiculo == null) {
            return null;
        }
        return VehiculoResumenDTO.builder()
                .id(vehiculo.getId())
                .placa(vehiculo.getPlaca())
                .tipo(vehiculo.getTipo())
                .estado(vehiculo.getEstado())
                .build();
    }

    private static ConductorResumenDTO toConductorResumenDTO(Conductor conductor) {
        return ConductorResumenDTO.builder()
                .id(conductor.getId())
                .nombre(conductor.getNombre())
                .apellido(conductor.getApellido())
                .turno(conductor.getTurno())
                .build();
    }
}
