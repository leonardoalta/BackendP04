package com.reportesciudadanos.service;

import com.reportesciudadanos.dto.request.ReporteStatusRequest;
import com.reportesciudadanos.dto.response.ReporteResponse;
import com.reportesciudadanos.dto.response.SeguimientoResponse;
import com.reportesciudadanos.entity.Seguimiento;
import com.reportesciudadanos.mapper.ReporteMapper;
import com.reportesciudadanos.mapper.SeguimientoMapper;
import com.reportesciudadanos.repository.SeguimientoRepository;
import com.reportesciudadanos.validation.TransitionPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SeguimientoService {
  private final SeguimientoRepository repository;
  private final SeguimientoMapper mapper;
  private final ReporteAccessService access;
  private final TransitionPolicy policy;
  private final CurrentUser current;
  private final ReporteMapper reports;

  @Transactional(readOnly = true)
  public java.util.List<SeguimientoResponse> history(Long id) {
    access.require(id);
    return repository.findByReporteIdOrderByFechaAscIdAsc(id).stream()
        .map(mapper::response)
        .toList();
  }

  @Transactional
  @PreAuthorize("@currentUser.hasAnyRole('OPERADOR','RESPONSABLE_ATENCION')")
  public ReporteResponse change(Long id, ReporteStatusRequest request) {
    var r = access.require(id);
    var role = current.role();
    policy.validate(r.getEstado(), request.estado(), role);
    var s = new Seguimiento();
    s.setReporte(r);
    s.setEstadoAnterior(r.getEstado());
    s.setEstadoNuevo(request.estado());
    s.setComentario(request.comentario());
    s.setActorRol(role.name());
    r.setEstado(request.estado());
    r.setFechaActualizacion(java.time.LocalDateTime.now());
    repository.save(s);
    return reports.response(r);
  }
}
