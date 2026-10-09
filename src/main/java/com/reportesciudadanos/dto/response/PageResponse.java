package com.reportesciudadanos.dto.response;

public record PageResponse<T>(
    java.util.List<T> content, int page, int size, long totalElements, int totalPages) {
  public static <T> PageResponse<T> of(org.springframework.data.domain.Page<T> p) {
    return new PageResponse<>(
        p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
  }
}
