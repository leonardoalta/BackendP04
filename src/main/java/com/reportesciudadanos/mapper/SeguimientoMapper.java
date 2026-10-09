package com.reportesciudadanos.mapper;

import com.reportesciudadanos.dto.response.*;
import com.reportesciudadanos.entity.Seguimiento;
import org.springframework.stereotype.Component;

@Component
public class SeguimientoMapper {
  public SeguimientoResponse response(Seguimiento e) {
    return new SeguimientoResponse(
        e.getId(),
        e.getEstadoAnterior(),
        e.getEstadoNuevo(),
        e.getComentario(),
        e.getActorRol(),
        e.getFecha());
  }
}
