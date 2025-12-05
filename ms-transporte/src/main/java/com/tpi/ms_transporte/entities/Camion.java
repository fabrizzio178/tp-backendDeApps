package com.tpi.ms_transporte.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "camion", schema = "logistica")
public class Camion {
    @Id
    @Column(name = "dominio_camion", length = 15)
    private String dominioCamion;

    @Column(name = "capacidad_peso", precision = 12, scale = 3)
    private BigDecimal capacidadPeso;

    @Column(name = "capacidad_volumen", precision = 12, scale = 3)
    private BigDecimal capacidadVolumen;

    @Column(name = "disponibilidad")
    private Boolean disponibilidad = Boolean.TRUE;

    @Column(name = "consumo_combustible", precision = 10, scale = 3)
    private BigDecimal consumoCombustible;

    @Column(name = "costo_base", precision = 12, scale = 2)
    private BigDecimal costoBase;

    @Column(name = "id_contenedor")
    private Long idContenedor; // referencia al contenedor (otro microservicio)

    @Column(name = "id_transportista", nullable = true)
    private Integer idTransportista;

    @Column(name = "latitud", precision = 9, scale = 6)
    private BigDecimal latitud;

    @Column(name = "longitud", precision = 9, scale = 6)
    private BigDecimal longitud;
}
