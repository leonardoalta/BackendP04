package com.reportesciudadanos.dto.request;

import jakarta.validation.constraints.*;

public record EvidenciaCreateRequest(
    @NotBlank @Size(max = 50) String tipo,
    @NotBlank @Size(max = 255) String referenciaFicticia,
    @Size(max = 255) String descripcion) {}
