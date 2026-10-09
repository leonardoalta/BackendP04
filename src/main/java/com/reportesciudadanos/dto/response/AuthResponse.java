package com.reportesciudadanos.dto.response;

import jakarta.validation.constraints.*;

public record AuthResponse(String accessToken, String tokenType, long expiresIn) {}
