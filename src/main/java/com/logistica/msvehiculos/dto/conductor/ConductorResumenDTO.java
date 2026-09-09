package com.logistica.msvehiculos.dto.conductor;

import com.logistica.msvehiculos.entity.Turno;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConductorResumenDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private Turno turno;
}
