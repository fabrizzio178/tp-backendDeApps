package com.tpi.ms_usuarios.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tpi.ms_usuarios.entities.Transportista;

@Repository
public interface TransportistaRepository extends JpaRepository<Transportista, Long> {
}
