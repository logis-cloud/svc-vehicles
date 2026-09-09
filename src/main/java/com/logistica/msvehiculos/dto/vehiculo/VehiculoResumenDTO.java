package com.logistica.msvehiculos.dto.vehiculo;

import com.logistica.msvehiculos.entity.EstadoVehiculo;
import com.logistica.msvehiculos.entity.TipoVehiculo;
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
public class VehiculoResumenDTO {

    private Long id;
    private String placa;
    private TipoVehiculo tipo;
    private EstadoVehiculo estado;
}
