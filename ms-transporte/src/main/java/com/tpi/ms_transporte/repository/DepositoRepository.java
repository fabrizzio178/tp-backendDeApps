package com.tpi.ms_transporte.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tpi.ms_transporte.entities.Deposito;

@Repository
public interface DepositoRepository extends JpaRepository<Deposito, Long> {

    List<Deposito> findByNombre(String nombre);
}

