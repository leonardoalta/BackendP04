package com.reportesciudadanos.dto.response;

import jakarta.validation.constraints.*;

public record CategoriaResponse(Long id, String nombre, String descripcion) {}
