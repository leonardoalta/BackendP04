package com.reportesciudadanos.controller;

import com.reportesciudadanos.dto.response.CategoriaResponse;
import com.reportesciudadanos.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
public class CategoriaController {
  private final CategoriaService service;

  @GetMapping
  public java.util.List<CategoriaResponse> list() {
    return service.active();
  }

  @GetMapping("/{id}")
  public CategoriaResponse get(@PathVariable Long id) {
    return service.get(id);
  }
}
