package com.tpi.microcontenedores.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "solicitud", schema = "logistica")
public class Solicitud {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud", nullable = false)
    private Long id;

    @Column(name = "costo_estimado", nullable = true)
    private Double costoEstimado;

    @Column(name = "tiempo_estimado", nullable = true)
    private Double tiempoEstimado; 

    @Column(name = "costo_final", nullable = true)
    private Integer costoFinal;

    @Column(name = "tiempo_real", nullable = true)
    private Integer tiempoReal;

    // Bueno le pregunte al blud y la manera de relacionarlo es de esta forma, no puedo tener relacioens. 
    // ASi me relaciono con otro microservicio en este caso el q tiene al cliente
    @Column(name = "id_cliente", nullable = false)
    private Integer idCliente;

    @OneToOne
    @JoinColumn(name = "id_contenedor", nullable = true)
    private Contenedor contenedor;

    @ManyToOne
    @JoinColumn(name = "id_estado", nullable = true)
    private Estado estado;

    @Column(name = "id_ruta", nullable = true)
    private Integer idRuta;

    @Column(name = "id_tarifa", nullable = true)
    private Integer idTarifa;

    @Column(name="fecha_creacion", nullable = true)
    private LocalDateTime fechaCreacion;

    @Column(name = "observaciones", nullable = true)
    private String observaciones;


}
