package com.reportesciudadanos.service;

import com.reportesciudadanos.entity.Reporte;
import com.reportesciudadanos.enums.RoleEnum;
import com.reportesciudadanos.exception.ApiException;
import com.reportesciudadanos.repository.ReporteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteAccessService {
  private final ReporteRepository repository;
  private final CurrentUser current;

  public Reporte require(Long id) {
    var r = repository.findById(id).orElseThrow(() -> ApiException.missing("reporte"));
    var u = current.get();
    if (u.getRol().getNombre() == RoleEnum.CIUDADANO
        && !r.getPropietario().getId().equals(u.getId()))
      throw new ApiException(403, "FORBIDDEN", "El reporte pertenece a otro ciudadano");
    return r;
  }
}
