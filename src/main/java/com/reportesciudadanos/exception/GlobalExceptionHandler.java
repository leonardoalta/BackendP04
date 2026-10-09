package com.reportesciudadanos.exception;

import com.reportesciudadanos.dto.response.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private ResponseEntity<ApiError> error(
      int status, String code, String message, HttpServletRequest r) {
    return ResponseEntity.status(status)
        .body(new ApiError(code, message, java.time.LocalDateTime.now(), r.getRequestURI()));
  }

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ApiError> api(ApiException e, HttpServletRequest r) {
    return error(e.getStatus(), e.getCode(), e.getMessage(), r);
  }

  @ExceptionHandler({
    MethodArgumentNotValidException.class,
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class,
    jakarta.validation.ConstraintViolationException.class
  })
  public ResponseEntity<ApiError> validation(Exception e, HttpServletRequest r) {
    return error(400, "INVALID_INPUT", "Los datos enviados no son válidos", r);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiError> forbidden(Exception e, HttpServletRequest r) {
    return error(403, "FORBIDDEN", "No tienes permiso para esta operación", r);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiError> unauthorized(Exception e, HttpServletRequest r) {
    return error(401, "UNAUTHORIZED", "Credenciales inválidas", r);
  }

  @ExceptionHandler({
    DataIntegrityViolationException.class,
    ObjectOptimisticLockingFailureException.class
  })
  public ResponseEntity<ApiError> conflict(Exception e, HttpServletRequest r) {
    return error(409, "CONFLICT", "La operación entra en conflicto con los datos actuales", r);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> unexpected(Exception e, HttpServletRequest r) {
    return error(500, "INTERNAL_ERROR", "No fue posible completar la operación", r);
  }
}
