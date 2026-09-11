package com.logistica.msvehiculos.repository;

import com.logistica.msvehiculos.entity.EstadoVehiculo;
import com.logistica.msvehiculos.entity.TipoVehiculo;
import com.logistica.msvehiculos.entity.Vehiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    Page<Vehiculo> findByTipo(TipoVehiculo tipo, Pageable pageable);

    Page<Vehiculo> findByEstado(EstadoVehiculo estado, Pageable pageable);

    Optional<Vehiculo> findByPlaca(String placa);

    boolean existsByPlaca(String placa);

    /**
     * Solo los IDs, sin cargar las entidades completas a memoria.
     * Usado por el seeder para asignar conductores a vehículos existentes
     * sin mantener 20,000+ entidades vivas en el contexto de persistencia.
     */
    @Query("select v.id from Vehiculo v")
    List<Long> findAllIds();
}
