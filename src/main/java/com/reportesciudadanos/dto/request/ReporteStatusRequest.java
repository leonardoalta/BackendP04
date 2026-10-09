package com.reportesciudadanos.dto.request;

import jakarta.validation.constraints.*;

public record ReporteStatusRequest(
    @NotNull com.reportesciudadanos.enums.EstadoReporte estado,
    @NotBlank @Size(max = 2000) String comentario) {}
