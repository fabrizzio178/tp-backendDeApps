package com.tpi.ms_tarifas.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tarifa", schema = "logistica")
public class Tarifa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tarifa")
    private Long id;

    @Column(name = "valor_costo_km_volumen", precision = 12, scale = 4)
    private BigDecimal valorCostoKmVolumen;

    @Column(name = "valor_litro", precision = 12, scale = 4)
    private BigDecimal valorLitro;

    @Column(name = "consumo_promedio", precision = 10, scale = 3)
    private BigDecimal consumoPromedio;

    // Relaciones a otros microservicios representadas por IDs/keys
    @Column(name = "id_deposito")
    private Long idDeposito;

    @Column(name = "dominio_camion", length = 15)
    private String dominioCamion;

    @Column(name = "id_contenedor")
    private Long idContenedor;

    @Column(name = "cargos_gestion", precision = 12, scale = 2)
    private BigDecimal cargosGestion;

    @Column(name = "fecha_vigencia")
    private LocalDate fechaVigencia;
}

