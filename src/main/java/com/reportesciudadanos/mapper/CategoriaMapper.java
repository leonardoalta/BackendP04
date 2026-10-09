package com.reportesciudadanos.mapper;

import com.reportesciudadanos.dto.response.*;
import com.reportesciudadanos.entity.Categoria;
import org.springframework.stereotype.Component;

@Component
public class CategoriaMapper {
  public CategoriaResponse response(Categoria e) {
    return new CategoriaResponse(e.getId(), e.getNombre(), e.getDescripcion());
  }
}
