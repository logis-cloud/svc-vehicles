package com.logistica.msvehiculos.service.impl;

import com.logistica.msvehiculos.dto.PageResponseDTO;
import com.logistica.msvehiculos.dto.conductor.ConductorResponseDTO;
import com.logistica.msvehiculos.dto.vehiculo.VehiculoRequestDTO;
import com.logistica.msvehiculos.dto.vehiculo.VehiculoResponseDTO;
import com.logistica.msvehiculos.dto.vehiculo.VehiculoUpdateDTO;
import com.logistica.msvehiculos.entity.EstadoVehiculo;
import com.logistica.msvehiculos.entity.TipoVehiculo;
import com.logistica.msvehiculos.entity.Vehiculo;
import com.logistica.msvehiculos.exception.BusinessException;
import com.logistica.msvehiculos.exception.ResourceNotFoundException;
import com.logistica.msvehiculos.mapper.ConductorMapper;
import com.logistica.msvehiculos.mapper.VehiculoMapper;
import com.logistica.msvehiculos.repository.ConductorRepository;
import com.logistica.msvehiculos.repository.VehiculoRepository;
import com.logistica.msvehiculos.service.VehiculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehiculoServiceImpl implements VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final ConductorRepository conductorRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<VehiculoResponseDTO> listar(int page, int size, TipoVehiculo tipo, EstadoVehiculo estado) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Page<Vehiculo> resultado;
        if (tipo != null) {
            resultado = vehiculoRepository.findByTipo(tipo, pageable);
        } else if (estado != null) {
            resultado = vehiculoRepository.findByEstado(estado, pageable);
        } else {
            resultado = vehiculoRepository.findAll(pageable);
        }

        List<VehiculoResponseDTO> contenido = resultado.getContent().stream()
                .map(v -> VehiculoMapper.toResponseDTO(v, false))
                .collect(Collectors.toList());

        return PageResponseDTO.<VehiculoResponseDTO>builder()
                .content(contenido)
                .page(resultado.getNumber())
                .size(resultado.getSize())
                .totalElements(resultado.getTotalElements())
                .totalPages(resultado.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculoResponseDTO obtenerPorId(Long id) {
        Vehiculo vehiculo = buscarOFallar(id);
        return VehiculoMapper.toResponseDTO(vehiculo, true);
    }

    @Override
    @Transactional
    public VehiculoResponseDTO crear(VehiculoRequestDTO dto) {
        String placa = dto.getPlaca().toUpperCase();
        if (vehiculoRepository.existsByPlaca(placa)) {
            throw new BusinessException("Ya existe un vehículo registrado con la placa " + placa);
        }
        Vehiculo vehiculo = VehiculoMapper.toEntity(dto);
        vehiculo = vehiculoRepository.save(vehiculo);
        return VehiculoMapper.toResponseDTO(vehiculo, false);
    }

    @Override
    @Transactional
    public VehiculoResponseDTO actualizar(Long id, VehiculoUpdateDTO dto) {
        Vehiculo vehiculo = buscarOFallar(id);
        String nuevaPlaca = dto.getPlaca().toUpperCase();
        if (!vehiculo.getPlaca().equals(nuevaPlaca) && vehiculoRepository.existsByPlaca(nuevaPlaca)) {
            throw new BusinessException("Ya existe un vehículo registrado con la placa " + nuevaPlaca);
        }
        VehiculoMapper.actualizarEntity(vehiculo, dto);
        return VehiculoMapper.toResponseDTO(vehiculoRepository.save(vehiculo), false);
    }

    @Override
    @Transactional
    public VehiculoResponseDTO cambiarEstado(Long id, EstadoVehiculo estado) {
        Vehiculo vehiculo = buscarOFallar(id);
        vehiculo.setEstado(estado);
        return VehiculoMapper.toResponseDTO(vehiculoRepository.save(vehiculo), false);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Vehiculo vehiculo = buscarOFallar(id);
        // Los conductores asignados NO se eliminan: la FK queda en NULL
        // (ON DELETE SET NULL definido en la entidad Conductor).
        vehiculoRepository.delete(vehiculo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConductorResponseDTO> listarConductoresDeVehiculo(Long id) {
        buscarOFallar(id);
        return conductorRepository.findByVehiculoId(id).stream()
                .map(ConductorMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    private Vehiculo buscarOFallar(Long id) {
        return vehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con id " + id));
    }
}
