package com.reportesciudadanos.controller;

import com.reportesciudadanos.dto.response.ZonaResponse;
import com.reportesciudadanos.service.ZonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/zonas")
@RequiredArgsConstructor
public class ZonaController {
  private final ZonaService service;

  @GetMapping
  public java.util.List<ZonaResponse> list() {
    return service.active();
  }

  @GetMapping("/{id}")
  public ZonaResponse get(@PathVariable Long id) {
    return service.get(id);
  }
}
