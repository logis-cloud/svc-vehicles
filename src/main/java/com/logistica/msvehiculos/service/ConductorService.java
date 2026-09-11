package com.logistica.msvehiculos.service;

import com.logistica.msvehiculos.dto.PageResponseDTO;
import com.logistica.msvehiculos.dto.conductor.ConductorRequestDTO;
import com.logistica.msvehiculos.dto.conductor.ConductorResponseDTO;
import com.logistica.msvehiculos.dto.conductor.ConductorUpdateDTO;
import com.logistica.msvehiculos.entity.Turno;

public interface ConductorService {

    PageResponseDTO<ConductorResponseDTO> listar(int page, int size, Turno turno, Long idVehiculo, Boolean activo);

    ConductorResponseDTO obtenerPorId(Long id);

    ConductorResponseDTO crear(ConductorRequestDTO dto);

    ConductorResponseDTO actualizar(Long id, ConductorUpdateDTO dto);

    ConductorResponseDTO asignarVehiculo(Long idConductor, Long idVehiculo);

    ConductorResponseDTO liberarVehiculo(Long idConductor);

    void eliminar(Long id);
}
