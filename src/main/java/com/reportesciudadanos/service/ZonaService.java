package com.reportesciudadanos.service;

import com.reportesciudadanos.dto.response.ZonaResponse;
import com.reportesciudadanos.entity.Zona;
import com.reportesciudadanos.exception.ApiException;
import com.reportesciudadanos.mapper.ZonaMapper;
import com.reportesciudadanos.repository.ZonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ZonaService {
  private final ZonaRepository repository;
  private final ZonaMapper mapper;

  public java.util.List<ZonaResponse> active() {
    return repository.findByActivaTrueOrderByNombreAsc().stream().map(mapper::response).toList();
  }

  public Zona require(Long id) {
    var e = repository.findById(id).orElseThrow(() -> ApiException.missing("zona"));
    if (!e.getActiva())
      throw new ApiException(409, "INACTIVE_RESOURCE", "El recurso está inactivo");
    return e;
  }

  public ZonaResponse get(Long id) {
    return mapper.response(require(id));
  }
}
