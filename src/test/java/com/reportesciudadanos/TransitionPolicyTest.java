package com.reportesciudadanos;

import static org.junit.jupiter.api.Assertions.*;

import com.reportesciudadanos.enums.EstadoReporte;
import com.reportesciudadanos.enums.RoleEnum;
import com.reportesciudadanos.exception.ApiException;
import com.reportesciudadanos.validation.TransitionPolicy;
import org.junit.jupiter.api.Test;

class TransitionPolicyTest {
  @Test
  void exhaustiveTransitionMatrix() {
    var policy = new TransitionPolicy();
    for (var from : EstadoReporte.values())
      for (var to : EstadoReporte.values())
        for (var role : RoleEnum.values()) {
          boolean edge =
              switch (from) {
                case REGISTRADO -> to == EstadoReporte.EN_REVISION;
                case EN_REVISION ->
                    to == EstadoReporte.REGISTRADO || to == EstadoReporte.EN_ATENCION;
                case EN_ATENCION -> to == EstadoReporte.EN_REVISION || to == EstadoReporte.RESUELTO;
                case RESUELTO -> to == EstadoReporte.EN_ATENCION || to == EstadoReporte.CERRADO;
                case CERRADO -> false;
              };
          boolean allowed =
              edge
                  && role != RoleEnum.CIUDADANO
                  && ((role == RoleEnum.OPERADOR)
                      == (from == EstadoReporte.REGISTRADO || from == EstadoReporte.EN_REVISION));
          if (allowed) assertDoesNotThrow(() -> policy.validate(from, to, role));
          else assertThrows(ApiException.class, () -> policy.validate(from, to, role));
        }
  }
}
