package com.reportesciudadanos.service;

import com.reportesciudadanos.dto.response.CategoriaResponse;
import com.reportesciudadanos.entity.Categoria;
import com.reportesciudadanos.exception.ApiException;
import com.reportesciudadanos.mapper.CategoriaMapper;
import com.reportesciudadanos.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoriaService {
  private final CategoriaRepository repository;
  private final CategoriaMapper mapper;

  public java.util.List<CategoriaResponse> active() {
    return repository.findByActivaTrueOrderByNombreAsc().stream().map(mapper::response).toList();
  }

  public Categoria require(Long id) {
    var e = repository.findById(id).orElseThrow(() -> ApiException.missing("categoria"));
    if (!e.getActiva())
      throw new ApiException(409, "INACTIVE_RESOURCE", "El recurso está inactivo");
    return e;
  }

  public CategoriaResponse get(Long id) {
    return mapper.response(require(id));
  }
}
