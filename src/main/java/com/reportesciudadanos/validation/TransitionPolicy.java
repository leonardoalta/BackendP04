package com.reportesciudadanos.validation;

import com.reportesciudadanos.enums.EstadoReporte;
import com.reportesciudadanos.enums.RoleEnum;
import com.reportesciudadanos.exception.ApiException;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class TransitionPolicy {
  private static final Map<EstadoReporte, Set<EstadoReporte>> ALLOWED =
      Map.of(
          EstadoReporte.REGISTRADO,
          Set.of(EstadoReporte.EN_REVISION),
          EstadoReporte.EN_REVISION,
          Set.of(EstadoReporte.REGISTRADO, EstadoReporte.EN_ATENCION),
          EstadoReporte.EN_ATENCION,
          Set.of(EstadoReporte.EN_REVISION, EstadoReporte.RESUELTO),
          EstadoReporte.RESUELTO,
          Set.of(EstadoReporte.EN_ATENCION, EstadoReporte.CERRADO),
          EstadoReporte.CERRADO,
          Set.of());

  public void validate(EstadoReporte from, EstadoReporte to, RoleEnum role) {
    if (role == RoleEnum.CIUDADANO)
      throw new ApiException(403, "FORBIDDEN", "No puedes cambiar estados");
    if (!ALLOWED.get(from).contains(to))
      throw new ApiException(
          409, "INVALID_TRANSITION", "La transición de estado no está permitida");
    boolean operator = from == EstadoReporte.REGISTRADO || from == EstadoReporte.EN_REVISION;
    if ((role == RoleEnum.OPERADOR) != operator)
      throw new ApiException(
          403, "FORBIDDEN_TRANSITION", "Tu rol no puede realizar esta transición");
  }
}
