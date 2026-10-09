package com.reportesciudadanos.dto.request;

import jakarta.validation.constraints.*;

public record ReporteCreateRequest(
    @NotNull @Positive Long categoriaId,
    @NotNull @Positive Long zonaId,
    @NotBlank @Size(max = 200) String titulo,
    @NotBlank @Size(max = 4000) String descripcion,
    @NotNull com.reportesciudadanos.enums.Prioridad prioridad) {}
