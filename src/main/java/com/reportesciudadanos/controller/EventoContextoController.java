package com.reportesciudadanos.controller;

import com.reportesciudadanos.dto.request.*;
import com.reportesciudadanos.dto.response.*;
import com.reportesciudadanos.service.EventoContextoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EventoContextoController {
  private final EventoContextoService service;

  @GetMapping("/reportes/{id}/eventos-contexto")
  public java.util.List<EventoContextoResponse> list(@PathVariable Long id) {
    return service.list(id);
  }

  @PostMapping("/eventos-contexto")
  public ResponseEntity<EventoContextoResponse> create(
      @Valid @RequestBody EventoContextoCreateRequest r) {
    return ResponseEntity.created(
            java.net.URI.create("/api/v1/reportes/" + r.reporteId() + "/eventos-contexto"))
        .body(service.create(r.reporteId(), r));
  }
}
