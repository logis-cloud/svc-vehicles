package com.logistica.msvehiculos.dto.conductor;

import com.logistica.msvehiculos.entity.Turno;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ConductorUpdateDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El número de licencia es obligatorio")
    private String nroLicencia;

    @NotBlank(message = "El DNI es obligatorio")
    private String dni;

    private String telefono;

    @NotNull(message = "El turno es obligatorio")
    private Turno turno;

    private LocalDate fechaContratacion;

    @NotNull(message = "El campo activo es obligatorio")
    private Boolean activo;
}
