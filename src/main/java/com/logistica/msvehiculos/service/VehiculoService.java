package com.logistica.msvehiculos.service;

import com.logistica.msvehiculos.dto.PageResponseDTO;
import com.logistica.msvehiculos.dto.conductor.ConductorResponseDTO;
import com.logistica.msvehiculos.dto.vehiculo.VehiculoRequestDTO;
import com.logistica.msvehiculos.dto.vehiculo.VehiculoResponseDTO;
import com.logistica.msvehiculos.dto.vehiculo.VehiculoUpdateDTO;
import com.logistica.msvehiculos.entity.EstadoVehiculo;
import com.logistica.msvehiculos.entity.TipoVehiculo;

import java.util.List;

public interface VehiculoService {

    PageResponseDTO<VehiculoResponseDTO> listar(int page, int size, TipoVehiculo tipo, EstadoVehiculo estado);

    VehiculoResponseDTO obtenerPorId(Long id);

    VehiculoResponseDTO crear(VehiculoRequestDTO dto);

    VehiculoResponseDTO actualizar(Long id, VehiculoUpdateDTO dto);

    VehiculoResponseDTO cambiarEstado(Long id, EstadoVehiculo estado);

    void eliminar(Long id);

    List<ConductorResponseDTO> listarConductoresDeVehiculo(Long id);
}
