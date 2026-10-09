package com.reportesciudadanos.dto.response;

import jakarta.validation.constraints.*;

public record EvidenciaResponse(
    Long id,
    String tipo,
    String referenciaFicticia,
    String descripcion,
    java.time.LocalDateTime fechaRegistro) {}
