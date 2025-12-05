package com.tpi.ms_tarifas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tpi.ms_tarifas.entities.Tarifa;

@Repository
public interface TarifaRepository extends JpaRepository<Tarifa, Long> {
}

