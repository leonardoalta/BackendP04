package com.reportesciudadanos.controller;

import com.reportesciudadanos.dto.response.SeguimientoResponse;
import com.reportesciudadanos.service.SeguimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reportes/{id}/seguimientos")
@RequiredArgsConstructor
public class SeguimientoController {
  private final SeguimientoService service;

  @GetMapping
  public java.util.List<SeguimientoResponse> history(@PathVariable Long id) {
    return service.history(id);
  }
}
