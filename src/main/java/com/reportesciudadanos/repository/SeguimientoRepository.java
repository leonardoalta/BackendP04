package com.reportesciudadanos.repository;

import com.reportesciudadanos.entity.Seguimiento;
import org.springframework.data.jpa.repository.*;

public interface SeguimientoRepository extends JpaRepository<Seguimiento, Long> {
  java.util.List<Seguimiento> findByReporteIdOrderByFechaAscIdAsc(Long id);
}
