package com.logistica.msvehiculos.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

@Entity
@Table(name = "conductores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conductor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conductor")
    private Long id;

    /**
     * FK opcional: un conductor puede no tener vehículo asignado (reserva /
     * pendiente de asignación). Si el vehículo se elimina, el registro del
     * conductor se conserva y este campo queda en NULL (ON DELETE SET NULL).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vehiculo")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Vehiculo vehiculo;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 80)
    private String apellido;

    @Column(name = "nro_licencia", nullable = false, unique = true, length = 20)
    private String nroLicencia;

    @Column(nullable = false, unique = true, length = 15)
    private String dni;

    @Column(length = 20)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Turno turno;

    @Column(name = "fecha_contratacion")
    private LocalDate fechaContratacion;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
