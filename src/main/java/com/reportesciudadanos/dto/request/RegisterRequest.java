package com.reportesciudadanos.dto.request;

import jakarta.validation.constraints.*;

public record RegisterRequest(
    @NotBlank @Size(max = 150) String nombre,
    @NotBlank @Email @Size(max = 254) String correoElectronico,
    @NotBlank @Size(min = 12, max = 72) String password) {
  @Override
  public String toString() {
    return "RegisterRequest[REDACTED]";
  }
}
