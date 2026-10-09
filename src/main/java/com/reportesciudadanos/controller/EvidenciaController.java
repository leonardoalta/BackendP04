package com.reportesciudadanos.controller;

import com.reportesciudadanos.dto.request.*;
import com.reportesciudadanos.dto.response.*;
import com.reportesciudadanos.service.EvidenciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EvidenciaController {
  private final EvidenciaService service;

  @GetMapping("/reportes/{id}/evidencias")
  public java.util.List<EvidenciaResponse> list(@PathVariable Long id) {
    return service.list(id);
  }

  @PostMapping("/reportes/{id}/evidencias")
  public ResponseEntity<EvidenciaResponse> create(
      @PathVariable Long id, @Valid @RequestBody EvidenciaCreateRequest r) {
    return ResponseEntity.created(java.net.URI.create("/api/v1/reportes/" + id + "/evidencias"))
        .body(service.create(id, r));
  }
}
