package com.reportesciudadanos.controller;

import com.reportesciudadanos.dto.request.*;
import com.reportesciudadanos.dto.response.*;
import com.reportesciudadanos.dto.response.PageResponse;
import com.reportesciudadanos.enums.*;
import com.reportesciudadanos.service.ReporteService;
import com.reportesciudadanos.service.SeguimientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
public class ReporteController {
  private final ReporteService service;
  private final SeguimientoService history;

  @PostMapping
  public ResponseEntity<ReporteResponse> create(@Valid @RequestBody ReporteCreateRequest r) {
    var response = service.create(r);
    return ResponseEntity.created(java.net.URI.create("/api/v1/reportes/" + response.id()))
        .body(response);
  }

  @GetMapping("/{id}")
  public ReporteDetailResponse get(@PathVariable Long id) {
    return service.get(id);
  }

  @GetMapping
  public PageResponse<ReporteSummaryResponse> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "fechaCreacion") String sort,
      @RequestParam(required = false) EstadoReporte estado,
      @RequestParam(required = false) Prioridad prioridad,
      @RequestParam(required = false) Long categoriaId,
      @RequestParam(required = false) Long zonaId) {
    return service.list(page, size, sort, estado, prioridad, categoriaId, zonaId, false);
  }

  @GetMapping("/mis-reportes")
  public PageResponse<ReporteSummaryResponse> mine(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return service.list(page, size, "fechaCreacion", null, null, null, null, true);
  }

  @PatchMapping("/{id}/estado")
  public ReporteResponse change(@PathVariable Long id, @Valid @RequestBody ReporteStatusRequest r) {
    return history.change(id, r);
  }
}
