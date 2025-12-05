package com.tpi.microcontenedores.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tpi.microcontenedores.entities.Estado;

@Repository
public interface EstadoRepository extends JpaRepository<Estado, Long> {
	Optional<Estado> findByNombreIgnoreCaseAndTipoEntidadIgnoreCase(String nombre, String tipoEntidad);
	List<Estado> findByTipoEntidadIgnoreCase(String tipoEntidad);
}
