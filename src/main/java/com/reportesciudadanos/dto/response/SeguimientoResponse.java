package com.reportesciudadanos.dto.response;

import jakarta.validation.constraints.*;

public record SeguimientoResponse(
    Long id,
    com.reportesciudadanos.enums.EstadoReporte estadoAnterior,
    com.reportesciudadanos.enums.EstadoReporte estadoNuevo,
    String comentario,
    String actorRol,
    java.time.LocalDateTime fecha) {}
