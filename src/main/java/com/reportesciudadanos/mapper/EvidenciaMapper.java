package com.reportesciudadanos.mapper;

import com.reportesciudadanos.dto.request.*;
import com.reportesciudadanos.dto.response.*;
import com.reportesciudadanos.entity.Evidencia;
import org.springframework.stereotype.Component;

@Component
public class EvidenciaMapper {
  public EvidenciaResponse response(Evidencia e) {
    return new EvidenciaResponse(
        e.getId(),
        e.getTipo(),
        e.getReferenciaFicticia(),
        e.getDescripcion(),
        e.getFechaRegistro());
  }
}
