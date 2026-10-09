package com.reportesciudadanos.service;

import com.reportesciudadanos.entity.Usuario;
import com.reportesciudadanos.enums.RoleEnum;
import com.reportesciudadanos.exception.ApiException;
import com.reportesciudadanos.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUser {
  private final UsuarioRepository users;

  public Usuario get() {
    var a = SecurityContextHolder.getContext().getAuthentication();
    if (a == null || !a.isAuthenticated())
      throw new ApiException(401, "UNAUTHORIZED", "Se requiere autenticación");
    return users
        .findByCorreoElectronico(a.getName())
        .filter(Usuario::isEnabled)
        .orElseThrow(() -> new ApiException(401, "UNAUTHORIZED", "Usuario no disponible"));
  }

  public boolean hasRole(String role) {
    return role().name().equals(role);
  }

  public boolean hasAnyRole(String... roles) {
    return java.util.Arrays.stream(roles).anyMatch(this::hasRole);
  }

  public RoleEnum role() {
    return get().getRol().getNombre();
  }
}
