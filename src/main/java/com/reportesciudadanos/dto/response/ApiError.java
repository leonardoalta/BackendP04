package com.reportesciudadanos.dto.response;

public record ApiError(
    String code, String message, java.time.LocalDateTime timestamp, String path) {}
