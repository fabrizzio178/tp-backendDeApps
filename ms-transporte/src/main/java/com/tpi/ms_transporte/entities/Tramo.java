package com.tpi.ms_transporte.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tramo", schema = "logistica")



public class Tramo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tramo")
    private Long id;

    @Column(name = "tipo", length = 50)
    private String tipo;

    @ManyToOne
    @JoinColumn(name = "id_punto", foreignKey = @ForeignKey(name = "tramo_id_punto_fkey"))
    private Punto punto;

    @ManyToOne
    @JoinColumn(name = "id_estado", foreignKey = @ForeignKey(name = "tramo_id_estado_fkey"))
    private Estado estado;

    @Column(name = "costo_real", precision = 12, scale = 2)
    private BigDecimal costoReal;

    @Column(name = "fecha_hora_inicio")
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_fin")
    private LocalDateTime fechaHoraFin;

    @ManyToOne
    @JoinColumn(name = "id_ruta", nullable = false, foreignKey = @ForeignKey(name = "tramo_id_ruta_fkey"))
    private Ruta ruta;

    @Column(name = "dominio_camion", length = 15)
    private String dominioCamion;

    @Column(name = "costo_estimado", precision = 12, scale = 2)
    private BigDecimal costoEstimado;

    @ManyToOne
    @JoinColumn(name = "id_punto_destino", foreignKey = @ForeignKey(name = "tramo_id_punto_destino_fkey"))
    private Punto puntoDestino;

}
