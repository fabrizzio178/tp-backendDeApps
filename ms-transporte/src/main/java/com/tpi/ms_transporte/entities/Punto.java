package com.tpi.ms_transporte.entities;

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
@Table(name = "punto_tramo", schema = "logistica")
public class Punto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_punto")
    private Long id;

    @Column(name = "tipo_punto", nullable = false, length = 50)
    private String tipoPunto;

    @Column(name = "longitud")
    private Double longitud;

    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "id_ciudad")
    private Integer idCiudad;

    @Column(name = "id_deposito")
    private Integer idDeposito;
}
