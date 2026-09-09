package com.logistica.msvehiculos.seeder;

import com.logistica.msvehiculos.entity.Conductor;
import com.logistica.msvehiculos.entity.EstadoVehiculo;
import com.logistica.msvehiculos.entity.TipoVehiculo;
import com.logistica.msvehiculos.entity.Turno;
import com.logistica.msvehiculos.entity.Vehiculo;
import com.logistica.msvehiculos.repository.ConductorRepository;
import com.logistica.msvehiculos.repository.VehiculoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Genera vehículos y conductores ficticios usando datafaker, en lotes,
 * para pruebas locales y para alimentar la ingesta del módulo de Data Science.
 *
 * Nota: placa, DNI y nro. de licencia se generan aleatoriamente; con los
 * volúmenes por defecto la probabilidad de colisión es muy baja, pero no
 * está garantizada. Es un seeder de desarrollo, no debe usarse en producción.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private static final int TAMANO_LOTE = 500;
    private static final String[] MARCAS = {"Toyota", "Hyundai", "Kia", "Nissan", "Volkswagen", "Mercedes-Benz", "Isuzu", "Honda"};
    private static final String[] MODELOS = {"Hilux", "H100", "Bongo", "NP300", "Crafter", "Sprinter", "NPR", "CG150"};

    private final VehiculoRepository vehiculoRepository;
    private final ConductorRepository conductorRepository;

    private final Faker faker = new Faker(new Locale("es"));
    private final Random random = new Random();

    @Transactional
    public Map<String, Integer> seed(int cantidadVehiculos, int cantidadConductores) {
        log.info("Iniciando seed: {} vehículos, {} conductores", cantidadVehiculos, cantidadConductores);

        List<Vehiculo> vehiculos = generarVehiculos(cantidadVehiculos);
        guardarEnLotes(vehiculos, vehiculoRepository::saveAll);

        List<Vehiculo> vehiculosPersistidos = vehiculoRepository.findAll();
        List<Conductor> conductores = generarConductores(cantidadConductores, vehiculosPersistidos);
        guardarEnLotes(conductores, conductorRepository::saveAll);

        Map<String, Integer> resultado = new LinkedHashMap<>();
        resultado.put("vehiculosCreados", vehiculos.size());
        resultado.put("conductoresCreados", conductores.size());
        log.info("Seed finalizado: {}", resultado);
        return resultado;
    }

    private List<Vehiculo> generarVehiculos(int cantidad) {
        List<Vehiculo> lista = new ArrayList<>(cantidad);
        TipoVehiculo[] tipos = TipoVehiculo.values();
        EstadoVehiculo[] estados = EstadoVehiculo.values();

        for (int i = 0; i < cantidad; i++) {
            TipoVehiculo tipo = tipos[random.nextInt(tipos.length)];
            lista.add(Vehiculo.builder()
                    .placa(generarPlaca())
                    .tipo(tipo)
                    .capacidadKg(capacidadSegunTipo(tipo))
                    .marca(MARCAS[random.nextInt(MARCAS.length)])
                    .modelo(MODELOS[random.nextInt(MODELOS.length)])
                    .anioFabricacion(2012 + random.nextInt(13))
                    .estado(estados[random.nextInt(estados.length)])
                    .build());
        }
        return lista;
    }

    private List<Conductor> generarConductores(int cantidad, List<Vehiculo> vehiculosDisponibles) {
        List<Conductor> lista = new ArrayList<>(cantidad);
        Turno[] turnos = Turno.values();

        for (int i = 0; i < cantidad; i++) {
            boolean seAsigna = !vehiculosDisponibles.isEmpty() && random.nextDouble() < 0.7;
            Vehiculo vehiculo = seAsigna
                    ? vehiculosDisponibles.get(random.nextInt(vehiculosDisponibles.size()))
                    : null;

            lista.add(Conductor.builder()
                    .nombre(faker.name().firstName())
                    .apellido(faker.name().lastName())
                    .nroLicencia(generarNumeroDe8Digitos())
                    .dni(generarNumeroDe8Digitos())
                    .telefono(faker.phoneNumber().phoneNumber())
                    .turno(turnos[random.nextInt(turnos.length)])
                    .fechaContratacion(LocalDate.now().minusDays(random.nextInt(365 * 5)))
                    .activo(random.nextDouble() < 0.9)
                    .vehiculo(vehiculo)
                    .build());
        }
        return lista;
    }

    private BigDecimal capacidadSegunTipo(TipoVehiculo tipo) {
        return switch (tipo) {
            case MOTO -> BigDecimal.valueOf(20 + random.nextInt(30));
            case FURGONETA -> BigDecimal.valueOf(500 + random.nextInt(1000));
            case CAMION -> BigDecimal.valueOf(3000 + random.nextInt(7000));
        };
    }

    private String generarPlaca() {
        return String.format("%s%s%s-%03d",
                (char) ('A' + random.nextInt(26)),
                (char) ('A' + random.nextInt(26)),
                (char) ('A' + random.nextInt(26)),
                random.nextInt(1000));
    }

    private String generarNumeroDe8Digitos() {
        return String.valueOf(10_000_000 + random.nextInt(89_999_999));
    }

    private <T> void guardarEnLotes(List<T> elementos, Consumer<List<T>> guardarFn) {
        for (int i = 0; i < elementos.size(); i += TAMANO_LOTE) {
            int fin = Math.min(i + TAMANO_LOTE, elementos.size());
            guardarFn.accept(elementos.subList(i, fin));
        }
    }
}
