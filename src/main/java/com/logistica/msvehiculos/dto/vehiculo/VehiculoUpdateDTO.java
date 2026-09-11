package com.logistica.msvehiculos.dto.vehiculo;

import com.logistica.msvehiculos.entity.TipoVehiculo;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehiculoUpdateDTO {

    @NotBlank(message = "La placa es obligatoria")
    @Pattern(regexp = "^[A-Za-z0-9-]{5,10}$", message = "Formato de placa inválido")
    private String placa;

    @NotNull(message = "El tipo de vehículo es obligatorio")
    private TipoVehiculo tipo;

    @NotNull(message = "La capacidad en kg es obligatoria")
    @DecimalMin(value = "0.1", message = "La capacidad debe ser mayor a 0")
    private BigDecimal capacidadKg;

    private String marca;

    private String modelo;

    @Min(value = 1980, message = "Año de fabricación inválido")
    private Integer anioFabricacion;
}
