package com.logistica.msvehiculos.repository;

import com.logistica.msvehiculos.entity.EstadoVehiculo;
import com.logistica.msvehiculos.entity.TipoVehiculo;
import com.logistica.msvehiculos.entity.Vehiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    Page<Vehiculo> findByTipo(TipoVehiculo tipo, Pageable pageable);

    Page<Vehiculo> findByEstado(EstadoVehiculo estado, Pageable pageable);

    Optional<Vehiculo> findByPlaca(String placa);

    boolean existsByPlaca(String placa);
}
