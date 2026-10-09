package com.reportesciudadanos.dto.response;

import jakarta.validation.constraints.*;

public record EventoContextoResponse(
    Long id,
    String fuente,
    java.time.LocalDateTime instante,
    String variable,
    String unidad,
    java.math.BigDecimal valor,
    String interpretacion) {}
