package com.reportesciudadanos.service;

import com.reportesciudadanos.dto.request.*;
import com.reportesciudadanos.dto.response.*;
import com.reportesciudadanos.entity.EventoContexto;
import com.reportesciudadanos.mapper.EventoContextoMapper;
import com.reportesciudadanos.repository.EventoContextoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventoContextoService {
  private final EventoContextoRepository repository;
  private final EventoContextoMapper mapper;
  private final ReporteAccessService access;

  @Transactional(readOnly = true)
  public java.util.List<EventoContextoResponse> list(Long id) {
    access.require(id);
    return repository.findByReporteIdOrderByInstanteAsc(id).stream().map(mapper::response).toList();
  }

  @Transactional
  @PreAuthorize("@currentUser.hasRole('RESPONSABLE_ATENCION')")
  public EventoContextoResponse create(Long id, EventoContextoCreateRequest r) {
    var report = access.require(id);
    var e = new EventoContexto();
    e.setReporte(report);
    e.setFuente(r.fuente());
    e.setVariable(r.variable());
    e.setUnidad(r.unidad());
    e.setValor(r.valor());
    e.setInterpretacion(r.interpretacion());
    return mapper.response(repository.save(e));
  }
}
