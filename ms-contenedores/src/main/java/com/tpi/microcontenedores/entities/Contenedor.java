package com.tpi.microcontenedores.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "contenedor", schema = "logistica")
public class Contenedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contenedor", nullable = false)
    private Long id;

    // numeric(12,3)
    @Column(name = "peso", nullable = false, precision = 12, scale = 3)
    private BigDecimal peso;

    // numeric(10,3)
    @Column(name = "altura", nullable = false, precision = 10, scale = 3)
    private BigDecimal altura;

    // numeric(10,3)
    @Column(name = "ancho", nullable = false, precision = 10, scale = 3)
    private BigDecimal ancho;

    // numeric(10,3)
    @Column(name = "largo", nullable = false, precision = 10, scale = 3)
    private BigDecimal largo;

    @ManyToOne
    @JoinColumn(name = "id_estado", foreignKey = @ForeignKey(name = "contenedor_id_estado_fkey"))
    private Estado estado;

    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;

    // Columna generada en DB (generated always ... stored). Solo lectura.
    @Column(name = "volumen", precision = 12, scale = 3, insertable = false, updatable = false)
    private BigDecimal volumen;

    // Campo de conveniencia para el POST: permitir { "idEstado": 1 }
    // No se persiste directamente; el service lo usa para resolver 'estado'.
    @Transient
    private Integer idEstado;
}
