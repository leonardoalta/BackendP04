package com.reportesciudadanos.mapper;

import com.reportesciudadanos.dto.response.*;
import com.reportesciudadanos.entity.Zona;
import org.springframework.stereotype.Component;

@Component
public class ZonaMapper {
  public ZonaResponse response(Zona e) {
    return new ZonaResponse(e.getId(), e.getNombre(), e.getReferencia());
  }
}
