package com.logistica.msvehiculos.mapper;

import com.logistica.msvehiculos.dto.conductor.ConductorRequestDTO;
import com.logistica.msvehiculos.dto.conductor.ConductorResponseDTO;
import com.logistica.msvehiculos.dto.conductor.ConductorUpdateDTO;
import com.logistica.msvehiculos.entity.Conductor;

public final class ConductorMapper {

    private ConductorMapper() {
    }

    public static Conductor toEntity(ConductorRequestDTO dto) {
        return Conductor.builder()
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .nroLicencia(dto.getNroLicencia())
                .dni(dto.getDni())
                .telefono(dto.getTelefono())
                .turno(dto.getTurno())
                .fechaContratacion(dto.getFechaContratacion())
                .activo(true)
                .build();
    }

    public static void actualizarEntity(Conductor conductor, ConductorUpdateDTO dto) {
        conductor.setNombre(dto.getNombre());
        conductor.setApellido(dto.getApellido());
        conductor.setNroLicencia(dto.getNroLicencia());
        conductor.setDni(dto.getDni());
        conductor.setTelefono(dto.getTelefono());
        conductor.setTurno(dto.getTurno());
        conductor.setFechaContratacion(dto.getFechaContratacion());
        conductor.setActivo(dto.getActivo());
    }

    public static ConductorResponseDTO toResponseDTO(Conductor conductor) {
        return ConductorResponseDTO.builder()
                .id(conductor.getId())
                .nombre(conductor.getNombre())
                .apellido(conductor.getApellido())
                .nroLicencia(conductor.getNroLicencia())
                .dni(conductor.getDni())
                .telefono(conductor.getTelefono())
                .turno(conductor.getTurno())
                .fechaContratacion(conductor.getFechaContratacion())
                .activo(conductor.getActivo())
                .vehiculo(VehiculoMapper.toResumenDTO(conductor.getVehiculo()))
                .build();
    }
}
