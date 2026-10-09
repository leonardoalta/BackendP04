package com.reportesciudadanos.repository;

import com.reportesciudadanos.entity.EventoContexto;
import org.springframework.data.jpa.repository.*;

public interface EventoContextoRepository extends JpaRepository<EventoContexto, Long> {
  java.util.List<EventoContexto> findByReporteIdOrderByInstanteAsc(Long id);
}
