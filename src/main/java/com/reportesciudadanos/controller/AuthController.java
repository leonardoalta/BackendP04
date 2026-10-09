package com.reportesciudadanos.controller;

import com.reportesciudadanos.dto.request.*;
import com.reportesciudadanos.dto.response.*;
import com.reportesciudadanos.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService service;

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest r) {
    return ResponseEntity.status(201).body(service.register(r));
  }

  @PostMapping("/login")
  public AuthResponse login(@Valid @RequestBody LoginRequest r) {
    return service.login(r);
  }
}
