package com.reportesciudadanos.dto.response;

import jakarta.validation.constraints.*;

public record ReporteResponse(
    Long id,
    Long categoriaId,
    Long zonaId,
    String titulo,
    String descripcion,
    com.reportesciudadanos.enums.EstadoReporte estado,
    com.reportesciudadanos.enums.Prioridad prioridad,
    java.time.LocalDateTime fechaCreacion,
    java.time.LocalDateTime fechaActualizacion) {}
