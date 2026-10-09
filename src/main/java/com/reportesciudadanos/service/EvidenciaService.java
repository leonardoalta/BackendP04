package com.reportesciudadanos.service;

import com.reportesciudadanos.dto.request.*;
import com.reportesciudadanos.dto.response.*;
import com.reportesciudadanos.entity.Evidencia;
import com.reportesciudadanos.mapper.EvidenciaMapper;
import com.reportesciudadanos.repository.EvidenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EvidenciaService {
  private final EvidenciaRepository repository;
  private final EvidenciaMapper mapper;
  private final ReporteAccessService access;

  @Transactional(readOnly = true)
  public java.util.List<EvidenciaResponse> list(Long id) {
    access.require(id);
    return repository.findByReporteIdOrderByFechaRegistroAsc(id).stream()
        .map(mapper::response)
        .toList();
  }

  @Transactional
  @PreAuthorize("@currentUser.hasRole('CIUDADANO')")
  public EvidenciaResponse create(Long id, EvidenciaCreateRequest r) {
    var report = access.require(id);
    var e = new Evidencia();
    e.setReporte(report);
    e.setTipo(r.tipo());
    e.setReferenciaFicticia(r.referenciaFicticia());
    e.setDescripcion(r.descripcion());
    return mapper.response(repository.save(e));
  }
}
