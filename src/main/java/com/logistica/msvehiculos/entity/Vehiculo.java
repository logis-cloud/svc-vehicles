package com.logistica.msvehiculos.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vehiculos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehiculo {

    /**
     * SEQUENCE en vez de IDENTITY: con IDENTITY, Hibernate necesita el ID
     * devuelto por la BD antes de poder encolar el siguiente insert, lo que
     * desactiva el batching (hibernate.jdbc.batch_size) por completo. Con
     * SEQUENCE (soportado nativamente por PostgreSQL) Hibernate sí puede
     * agrupar los inserts en lotes, algo necesario para que el seed de
     * 20,000+ registros sea rápido.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vehiculo_seq")
    @SequenceGenerator(name = "vehiculo_seq", sequenceName = "vehiculos_id_vehiculo_seq", allocationSize = 50)
    @Column(name = "id_vehiculo")
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String placa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoVehiculo tipo;

    @Column(name = "capacidad_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal capacidadKg;

    @Column(length = 50)
    private String marca;

    @Column(length = 50)
    private String modelo;

    @Column(name = "anio_fabricacion")
    private Integer anioFabricacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoVehiculo estado = EstadoVehiculo.DISPONIBLE;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    /**
     * Un vehículo puede tener varios conductores a lo largo del tiempo
     * (rotación por turno). Ver decisión de diseño en el README.
     */
    @OneToMany(mappedBy = "vehiculo")
    @Builder.Default
    private List<Conductor> conductores = new ArrayList<>();
}
