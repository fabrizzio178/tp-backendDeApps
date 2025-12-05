package com.tpi.ms_transporte.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tpi.ms_transporte.entities.Punto;

@Repository
public interface PuntoRepository extends JpaRepository<Punto, Long> {

    List<Punto> findByIdDeposito(Long idDeposito);

    List<Punto> findByLatitudAndLongitud(double latitud, double longitud);

}
