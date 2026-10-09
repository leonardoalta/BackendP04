package com.reportesciudadanos.dto.request;

import jakarta.validation.constraints.*;

public record LoginRequest(
    @NotBlank @Email String correoElectronico, @NotBlank @Size(max = 72) String password) {
  @Override
  public String toString() {
    return "LoginRequest[REDACTED]";
  }
}
