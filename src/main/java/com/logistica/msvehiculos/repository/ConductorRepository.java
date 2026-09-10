package com.logistica.msvehiculos.repository;

import com.logistica.msvehiculos.entity.Conductor;
import com.logistica.msvehiculos.entity.Turno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConductorRepository extends JpaRepository<Conductor, Long> {

    Page<Conductor> findByTurno(Turno turno, Pageable pageable);

    Page<Conductor> findByVehiculoId(Long idVehiculo, Pageable pageable);

    List<Conductor> findByVehiculoId(Long idVehiculo);

    Page<Conductor> findByActivo(Boolean activo, Pageable pageable);

    Optional<Conductor> findByNroLicencia(String nroLicencia);

    Optional<Conductor> findByDni(String dni);

    boolean existsByNroLicencia(String nroLicencia);

    boolean existsByDni(String dni);
}
