package com.tpi.ms_transporte.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Table(name = "deposito", schema = "logistica")

public class Deposito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_deposito")
    private Long id;
    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;
    @Column(name = "costo_estadia", precision = 12, scale = 2)
    private BigDecimal costoEstadia;
    @Column(name = "latitud", precision = 9, scale = 6)
    private BigDecimal latitud;
    @Column(name = "longitud", precision = 9, scale = 6)
    private BigDecimal longitud;
    @Column(name = "direccion", length = 150)
    private String direccion;

}
