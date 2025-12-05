package com.tpi.ms_transporte.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tpi.ms_transporte.entities.Tramo;

@Repository
public interface TramoRepository extends JpaRepository<Tramo, Long> {
    List<Tramo> findByRutaId(Long idRuta);

}
