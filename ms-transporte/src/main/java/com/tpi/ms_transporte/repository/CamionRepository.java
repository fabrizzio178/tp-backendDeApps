package com.tpi.ms_transporte.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tpi.ms_transporte.entities.Camion;

@Repository
public interface CamionRepository extends JpaRepository<Camion, String> {}
