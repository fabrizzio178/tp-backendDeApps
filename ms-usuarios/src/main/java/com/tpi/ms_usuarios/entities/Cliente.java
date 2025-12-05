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
 CREATE TABLE logistica.cliente (
    id_cliente integer NOT NULL,
    nombre character varying(100) NOT NULL,
    apellido character varying(100) NOT NULL,
    dni character varying(20),
    mail character varying(150),
    numero character varying(30)
);
  
*/

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "cliente", schema = "logistica")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente", nullable = false)
    private Long id;
    
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;

    @Column(name = "dni", nullable = true, length = 20)
    private String dni;

    @Column(name = "mail", nullable = true, length = 150)
    private String mail;

    @Column(name = "numero", nullable = true, length = 30)
    private String numero;
}
