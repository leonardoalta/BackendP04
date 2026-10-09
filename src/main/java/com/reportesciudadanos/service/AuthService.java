package com.reportesciudadanos.service;

import com.reportesciudadanos.dto.request.*;
import com.reportesciudadanos.dto.response.*;
import com.reportesciudadanos.entity.Usuario;
import com.reportesciudadanos.enums.RoleEnum;
import com.reportesciudadanos.exception.ApiException;
import com.reportesciudadanos.repository.RolRepository;
import com.reportesciudadanos.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final UsuarioRepository users;
  private final RolRepository roles;
  private final PasswordEncoder passwords;
  private final JwtService jwt;

  @Transactional
  public AuthResponse register(RegisterRequest r) {
    String email = r.correoElectronico().trim().toLowerCase(java.util.Locale.ROOT);
    if (users.findByCorreoElectronico(email).isPresent())
      throw new ApiException(409, "EMAIL_EXISTS", "El correo ya está registrado");
    if (r.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
      throw new ApiException(400, "INVALID_PASSWORD", "La contraseña supera 72 bytes");
    var u = new Usuario();
    u.setNombre(r.nombre().trim());
    u.setCorreoElectronico(email);
    u.setPasswordHash(passwords.encode(r.password()));
    u.setRol(roles.findByNombre(RoleEnum.CIUDADANO).orElseThrow(() -> ApiException.missing("rol")));
    return jwt.issue(users.save(u));
  }

  @Transactional(readOnly = true)
  public AuthResponse login(LoginRequest r) {
    var u =
        users
            .findByCorreoElectronico(
                r.correoElectronico().trim().toLowerCase(java.util.Locale.ROOT))
            .orElse(null);
    String hash =
        u == null
            ? "$2a$10$7EqJtq98hPqEX7fNZaFWoO5mY5nA0zXvfPcOvV5NQOcPQf5qv9D1e"
            : u.getPasswordHash();
    boolean matches = passwords.matches(r.password(), hash);
    if (u == null || !u.isEnabled() || !matches)
      throw new ApiException(401, "INVALID_CREDENTIALS", "Credenciales inválidas");
    return jwt.issue(u);
  }
}
