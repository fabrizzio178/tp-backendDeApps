package com.tpi.ms_usuarios.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 Completar con los datos de creacion de la base de datos
 CREATE TABLE logistica.transportista (
    id_transportista integer NOT NULL,
    nombre character varying(120) NOT NULL,
    telefono character varying(30)
);
);
  
*/

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "transportista", schema = "logistica")
public class Transportista {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transportista", nullable = false)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "telefono", nullable = true, length = 30)
    private String telefono;
    
}
