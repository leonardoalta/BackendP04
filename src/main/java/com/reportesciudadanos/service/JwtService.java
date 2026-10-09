package com.reportesciudadanos.service;

import com.reportesciudadanos.dto.response.AuthResponse;
import com.reportesciudadanos.entity.Usuario;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {
  private final JwtEncoder encoder;

  @Value("${app.jwt.ttl-seconds}")
  private long ttl;

  public AuthResponse issue(Usuario u) {
    var now = Instant.now();
    var claims =
        JwtClaimsSet.builder()
            .issuer("reportes-ciudadanos")
            .subject(u.getCorreoElectronico())
            .issuedAt(now)
            .expiresAt(now.plusSeconds(ttl))
            .claim("roles", java.util.List.of(u.getRol().getNombre().name()))
            .build();
    return new AuthResponse(
        encoder
            .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
            .getTokenValue(),
        "Bearer",
        ttl);
  }
}
