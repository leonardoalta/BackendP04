package com.reportesciudadanos.mapper;

import com.reportesciudadanos.dto.request.*;
import com.reportesciudadanos.dto.response.*;
import com.reportesciudadanos.entity.EventoContexto;
import org.springframework.stereotype.Component;

@Component
public class EventoContextoMapper {
  public EventoContextoResponse response(EventoContexto e) {
    return new EventoContextoResponse(
        e.getId(),
        e.getFuente(),
        e.getInstante(),
        e.getVariable(),
        e.getUnidad(),
        e.getValor(),
        e.getInterpretacion());
  }
}
