package com.reportesciudadanos.dto.response;

import jakarta.validation.constraints.*;

public record ReporteSummaryResponse(
    Long id,
    String titulo,
    com.reportesciudadanos.enums.EstadoReporte estado,
    com.reportesciudadanos.enums.Prioridad prioridad,
    java.time.LocalDateTime fechaCreacion) {}
