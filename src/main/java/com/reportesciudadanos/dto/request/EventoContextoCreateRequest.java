package com.reportesciudadanos.dto.request;

import jakarta.validation.constraints.*;

public record EventoContextoCreateRequest(
    @NotNull @Positive Long reporteId,
    @NotBlank @Size(max = 255) String fuente,
    @NotBlank @Size(max = 255) String variable,
    @NotBlank @Size(max = 255) String unidad,
    @NotNull @Digits(integer = 13, fraction = 6) java.math.BigDecimal valor,
    @Size(max = 255) String interpretacion) {}
