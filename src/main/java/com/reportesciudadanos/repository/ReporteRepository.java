package com.reportesciudadanos.repository;

import com.reportesciudadanos.entity.Reporte;
import org.springframework.data.jpa.repository.*;

public interface ReporteRepository
    extends JpaRepository<Reporte, Long>, JpaSpecificationExecutor<Reporte> {}
