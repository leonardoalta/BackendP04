package com.reportesciudadanos.dto.response;

import jakarta.validation.constraints.*;

public record ReporteDetailResponse(
    ReporteResponse reporte,
    java.util.List<com.reportesciudadanos.dto.response.EvidenciaResponse> evidencias,
    java.util.List<com.reportesciudadanos.dto.response.SeguimientoResponse> seguimientos,
    java.util.List<com.reportesciudadanos.dto.response.EventoContextoResponse> eventosContexto) {}
