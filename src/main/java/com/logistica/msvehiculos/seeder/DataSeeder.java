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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Genera vehículos y conductores ficticios usando datafaker, en lotes,
 * para pruebas locales y para alimentar la ingesta del módulo de Data Science.
 *
 * IMPORTANTE (alineado con el requisito de la rúbrica de insertar masivamente,
 * por única vez, un mínimo de 20,000 registros en al menos 1 tabla por base de
 * datos SQL): los defaults de este seeder generan 20,000+ conductores, que es
 * la tabla que cumple ese mínimo en esta base de datos PostgreSQL (ver README).
 *
 * NOTA DE DISEÑO (corrección respecto a la versión anterior): a este volumen,
 * generar DNI/nro. de licencia/placa completamente al azar sin control de
 * unicidad tiene una probabilidad no despreciable de colisión (problema del
 * cumpleaños: con 20,000 muestras sobre ~90 millones de combinaciones posibles
 * se esperan ~2 colisiones), y como estos campos son UNIQUE en la base de
 * datos, una sola colisión rompía el lote completo. Por eso aquí se llevan
 * Sets en memoria para garantizar unicidad dentro de la corrida del seeder.
 *
 * También se elimina el @Transactional de método único: igual que el script
 * seed_data.py del microservicio de Clientes (que hace commit cada 1,000
 * registros), aquí cada lote se guarda y confirma independientemente, para
 * no arriesgar una transacción gigante de 20,000+ filas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private static final int TAMANO_LOTE = 1_000;
    private static final String[] MARCAS = {"Toyota", "Hyundai", "Kia", "Nissan", "Volkswagen", "Mercedes-Benz", "Isuzu", "Honda"};
    private static final String[] MODELOS = {"Hilux", "H100", "Bongo", "NP300", "Crafter", "Sprinter", "NPR", "CG150"};

    /** Límite defensivo para evitar que alguien tumbe la BD con un query param absurdo. */
    private static final int MAX_POR_LLAMADA = 200_000;

    private final VehiculoRepository vehiculoRepository;
    private final ConductorRepository conductorRepository;

    private final Faker faker = new Faker(new Locale("es"));
    private final Random random = new Random();

    public Map<String, Integer> seed(int cantidadVehiculos, int cantidadConductores) {
        cantidadVehiculos = Math.min(Math.max(cantidadVehiculos, 0), MAX_POR_LLAMADA);
        cantidadConductores = Math.min(Math.max(cantidadConductores, 0), MAX_POR_LLAMADA);

        log.info("Iniciando seed: {} vehículos, {} conductores", cantidadVehiculos, cantidadConductores);

        Set<String> placasUsadas = new HashSet<>();
        Set<String> dnisUsados = new HashSet<>();
        Set<String> licenciasUsadas = new HashSet<>();

        int vehiculosCreados = generarYGuardarVehiculos(cantidadVehiculos, placasUsadas);

        // Se recargan los IDs persistidos (no los objetos en memoria del lote anterior)
        // para poder asignar vehículo a los conductores sin mantener 20k+ entidades vivas.
        List<Long> idsVehiculos = vehiculoRepository.findAllIds();

        int conductoresCreados = generarYGuardarConductores(
                cantidadConductores, idsVehiculos, dnisUsados, licenciasUsadas
        );

        Map<String, Integer> resultado = new LinkedHashMap<>();
        resultado.put("vehiculosCreados", vehiculosCreados);
        resultado.put("conductoresCreados", conductoresCreados);
        log.info("Seed finalizado: {}", resultado);
        return resultado;
    }

    private int generarYGuardarVehiculos(int cantidad, Set<String> placasUsadas) {
        TipoVehiculo[] tipos = TipoVehiculo.values();
        EstadoVehiculo[] estados = EstadoVehiculo.values();
        List<Vehiculo> lote = new ArrayList<>(TAMANO_LOTE);
        int total = 0;

        for (int i = 0; i < cantidad; i++) {
            TipoVehiculo tipo = tipos[random.nextInt(tipos.length)];
            lote.add(Vehiculo.builder()
                    .placa(generarPlacaUnica(placasUsadas))
                    .tipo(tipo)
                    .capacidadKg(capacidadSegunTipo(tipo))
                    .marca(MARCAS[random.nextInt(MARCAS.length)])
                    .modelo(MODELOS[random.nextInt(MODELOS.length)])
                    .anioFabricacion(2012 + random.nextInt(13))
                    .estado(estados[random.nextInt(estados.length)])
                    .build());

            if (lote.size() >= TAMANO_LOTE) {
                vehiculoRepository.saveAll(lote);
                total += lote.size();
                log.info("Vehículos insertados: {}/{}", total, cantidad);
                lote.clear();
            }
        }
        if (!lote.isEmpty()) {
            vehiculoRepository.saveAll(lote);
            total += lote.size();
            log.info("Vehículos insertados: {}/{}", total, cantidad);
        }
        return total;
    }

    private int generarYGuardarConductores(
            int cantidad, List<Long> idsVehiculosDisponibles,
            Set<String> dnisUsados, Set<String> licenciasUsadas
    ) {
        Turno[] turnos = Turno.values();
        List<Conductor> lote = new ArrayList<>(TAMANO_LOTE);
        int total = 0;
        boolean hayVehiculos = idsVehiculosDisponibles != null && !idsVehiculosDisponibles.isEmpty();

        for (int i = 0; i < cantidad; i++) {
            boolean seAsigna = hayVehiculos && random.nextDouble() < 0.7;
            Vehiculo vehiculoRef = null;
            if (seAsigna) {
                Long idVehiculo = idsVehiculosDisponibles.get(random.nextInt(idsVehiculosDisponibles.size()));
                // getReferenceById evita traer la entidad completa a memoria para cada conductor.
                vehiculoRef = vehiculoRepository.getReferenceById(idVehiculo);
            }

            lote.add(Conductor.builder()
                    .nombre(faker.name().firstName())
                    .apellido(faker.name().lastName())
                    .nroLicencia(generarNumeroUnico(licenciasUsadas))
                    .dni(generarNumeroUnico(dnisUsados))
                    .telefono(faker.phoneNumber().phoneNumber())
                    .turno(turnos[random.nextInt(turnos.length)])
                    .fechaContratacion(LocalDate.now().minusDays(random.nextInt(365 * 5)))
                    .activo(random.nextDouble() < 0.9)
                    .vehiculo(vehiculoRef)
                    .build());

            if (lote.size() >= TAMANO_LOTE) {
                conductorRepository.saveAll(lote);
                total += lote.size();
                log.info("Conductores insertados: {}/{}", total, cantidad);
                lote.clear();
            }
        }
        if (!lote.isEmpty()) {
            conductorRepository.saveAll(lote);
            total += lote.size();
            log.info("Conductores insertados: {}/{}", total, cantidad);
        }
        return total;
    }

    private BigDecimal capacidadSegunTipo(TipoVehiculo tipo) {
        return switch (tipo) {
            case MOTO -> BigDecimal.valueOf(20 + random.nextInt(30));
            case FURGONETA -> BigDecimal.valueOf(500 + random.nextInt(1000));
            case CAMION -> BigDecimal.valueOf(3000 + random.nextInt(7000));
        };
    }

    private String generarPlacaUnica(Set<String> usadas) {
        String placa;
        do {
            placa = String.format("%s%s%s-%03d",
                    (char) ('A' + random.nextInt(26)),
                    (char) ('A' + random.nextInt(26)),
                    (char) ('A' + random.nextInt(26)),
                    random.nextInt(1000));
        } while (!usadas.add(placa));
        return placa;
    }

    /** Genera un número de 8 dígitos garantizando que no se repita dentro de esta corrida del seeder. */
    private String generarNumeroUnico(Set<String> usados) {
        String numero;
        do {
            numero = String.valueOf(10_000_000 + random.nextInt(89_999_999));
        } while (!usados.add(numero));
        return numero;
    }

}
