package com.logistica.msvehiculos.dto.conductor;

import com.logistica.msvehiculos.dto.vehiculo.VehiculoResumenDTO;
import com.logistica.msvehiculos.entity.Turno;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConductorResponseDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String nroLicencia;
    private String dni;
    private String telefono;
    private Turno turno;
    private LocalDate fechaContratacion;
    private Boolean activo;

    /** null si el conductor no tiene vehículo asignado actualmente. */
    private VehiculoResumenDTO vehiculo;
}
