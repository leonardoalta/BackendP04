package com.reportesciudadanos.repository;

import com.reportesciudadanos.entity.Zona;
import org.springframework.data.jpa.repository.*;

public interface ZonaRepository extends JpaRepository<Zona, Long> {
  java.util.List<Zona> findByActivaTrueOrderByNombreAsc();
}
