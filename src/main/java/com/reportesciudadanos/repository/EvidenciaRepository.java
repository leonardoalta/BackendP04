package com.reportesciudadanos.repository;

import com.reportesciudadanos.entity.Evidencia;
import org.springframework.data.jpa.repository.*;

public interface EvidenciaRepository extends JpaRepository<Evidencia, Long> {
  java.util.List<Evidencia> findByReporteIdOrderByFechaRegistroAsc(Long id);
}
