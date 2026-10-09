package com.reportesciudadanos.mapper;

import com.reportesciudadanos.dto.request.*;
import com.reportesciudadanos.dto.response.*;
import com.reportesciudadanos.entity.Reporte;
import org.springframework.stereotype.Component;

@Component
public class ReporteMapper {
  public ReporteResponse response(Reporte e) {
    return new ReporteResponse(
        e.getId(),
        e.getCategoria().getId(),
        e.getZona().getId(),
        e.getTitulo(),
        e.getDescripcion(),
        e.getEstado(),
        e.getPrioridad(),
        e.getFechaCreacion(),
        e.getFechaActualizacion());
  }

  public ReporteSummaryResponse summary(Reporte e) {
    return new ReporteSummaryResponse(
        e.getId(), e.getTitulo(), e.getEstado(), e.getPrioridad(), e.getFechaCreacion());
  }
}
