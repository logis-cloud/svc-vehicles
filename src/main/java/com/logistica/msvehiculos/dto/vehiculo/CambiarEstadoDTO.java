package com.logistica.msvehiculos.dto.vehiculo;

import com.logistica.msvehiculos.entity.EstadoVehiculo;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CambiarEstadoDTO {

    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoVehiculo estado;
}
