package com.logistica.msvehiculos.service.impl;

import com.logistica.msvehiculos.dto.PageResponseDTO;
import com.logistica.msvehiculos.dto.conductor.ConductorRequestDTO;
import com.logistica.msvehiculos.dto.conductor.ConductorResponseDTO;
import com.logistica.msvehiculos.dto.conductor.ConductorUpdateDTO;
import com.logistica.msvehiculos.entity.Conductor;
import com.logistica.msvehiculos.entity.EstadoVehiculo;
import com.logistica.msvehiculos.entity.Turno;
import com.logistica.msvehiculos.entity.Vehiculo;
import com.logistica.msvehiculos.exception.BusinessException;
import com.logistica.msvehiculos.exception.ResourceNotFoundException;
import com.logistica.msvehiculos.mapper.ConductorMapper;
import com.logistica.msvehiculos.repository.ConductorRepository;
import com.logistica.msvehiculos.repository.VehiculoRepository;
import com.logistica.msvehiculos.service.ConductorService;
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
public class ConductorServiceImpl implements ConductorService {

    private final ConductorRepository conductorRepository;
    private final VehiculoRepository vehiculoRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ConductorResponseDTO> listar(int page, int size, Turno turno, Long idVehiculo, Boolean activo) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Page<Conductor> resultado;
        if (turno != null) {
            resultado = conductorRepository.findByTurno(turno, pageable);
        } else if (idVehiculo != null) {
            resultado = conductorRepository.findByVehiculoId(idVehiculo, pageable);
        } else if (activo != null) {
            resultado = conductorRepository.findByActivo(activo, pageable);
        } else {
            resultado = conductorRepository.findAll(pageable);
        }

        List<ConductorResponseDTO> contenido = resultado.getContent().stream()
                .map(ConductorMapper::toResponseDTO)
                .collect(Collectors.toList());

        return PageResponseDTO.<ConductorResponseDTO>builder()
                .content(contenido)
                .page(resultado.getNumber())
                .size(resultado.getSize())
                .totalElements(resultado.getTotalElements())
                .totalPages(resultado.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ConductorResponseDTO obtenerPorId(Long id) {
        return ConductorMapper.toResponseDTO(buscarOFallar(id));
    }

    @Override
    @Transactional
    public ConductorResponseDTO crear(ConductorRequestDTO dto) {
        validarUnicidad(dto.getNroLicencia(), dto.getDni(), null);

        Conductor conductor = ConductorMapper.toEntity(dto);

        if (dto.getIdVehiculo() != null) {
            conductor.setVehiculo(obtenerVehiculoAsignable(dto.getIdVehiculo()));
        }

        return ConductorMapper.toResponseDTO(conductorRepository.save(conductor));
    }

    @Override
    @Transactional
    public ConductorResponseDTO actualizar(Long id, ConductorUpdateDTO dto) {
        Conductor conductor = buscarOFallar(id);
        validarUnicidad(dto.getNroLicencia(), dto.getDni(), id);
        ConductorMapper.actualizarEntity(conductor, dto);
        return ConductorMapper.toResponseDTO(conductorRepository.save(conductor));
    }

    @Override
    @Transactional
    public ConductorResponseDTO asignarVehiculo(Long idConductor, Long idVehiculo) {
        Conductor conductor = buscarOFallar(idConductor);
        conductor.setVehiculo(obtenerVehiculoAsignable(idVehiculo));
        return ConductorMapper.toResponseDTO(conductorRepository.save(conductor));
    }

    @Override
    @Transactional
    public ConductorResponseDTO liberarVehiculo(Long idConductor) {
        Conductor conductor = buscarOFallar(idConductor);
        conductor.setVehiculo(null);
        return ConductorMapper.toResponseDTO(conductorRepository.save(conductor));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Conductor conductor = buscarOFallar(id);
        conductorRepository.delete(conductor);
    }

    private Vehiculo obtenerVehiculoAsignable(Long idVehiculo) {
        Vehiculo vehiculo = vehiculoRepository.findById(idVehiculo)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con id " + idVehiculo));
        if (vehiculo.getEstado() == EstadoVehiculo.INACTIVO) {
            throw new BusinessException("No se puede asignar un vehículo en estado INACTIVO");
        }
        return vehiculo;
    }

    private void validarUnicidad(String nroLicencia, String dni, Long idAExcluir) {
        conductorRepository.findByNroLicencia(nroLicencia).ifPresent(existente -> {
            if (!existente.getId().equals(idAExcluir)) {
                throw new BusinessException("Ya existe un conductor con el número de licencia " + nroLicencia);
            }
        });
        conductorRepository.findByDni(dni).ifPresent(existente -> {
            if (!existente.getId().equals(idAExcluir)) {
                throw new BusinessException("Ya existe un conductor con el DNI " + dni);
            }
        });
    }

    private Conductor buscarOFallar(Long id) {
        return conductorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conductor no encontrado con id " + id));
    }
}
