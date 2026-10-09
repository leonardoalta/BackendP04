package com.reportesciudadanos.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.reportesciudadanos.dto.response.ApiError;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  private SecretKeySpec key(String secret) {
    byte[] bytes = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    if (bytes.length < 32)
      throw new IllegalArgumentException("JWT_SECRET requiere al menos 32 bytes");
    return new SecretKeySpec(bytes, "HmacSHA256");
  }

  @Bean
  JwtEncoder jwtEncoder(@Value("${app.jwt.secret}") String s) {
    return new NimbusJwtEncoder(new ImmutableSecret<>(key(s)));
  }

  @Bean
  JwtDecoder jwtDecoder(@Value("${app.jwt.secret}") String s) {
    var d = NimbusJwtDecoder.withSecretKey(key(s)).macAlgorithm(MacAlgorithm.HS256).build();
    d.setJwtValidator(JwtValidators.createDefaultWithIssuer("reportes-ciudadanos"));
    return d;
  }

  @Bean
  CorsConfigurationSource cors(@Value("${app.cors-origins}") String origins) {
    var c = new CorsConfiguration();
    c.setAllowedOrigins(java.util.Arrays.asList(origins.split(",")));
    c.setAllowedMethods(java.util.List.of("GET", "POST", "PATCH", "OPTIONS"));
    c.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type"));
    var s = new UrlBasedCorsConfigurationSource();
    s.registerCorsConfiguration("/**", c);
    return s;
  }

  @Bean
  SecurityFilterChain filter(HttpSecurity http, ObjectMapper mapper) throws Exception {
    var converter = new JwtAuthenticationConverter();
    var roles = new JwtGrantedAuthoritiesConverter();
    roles.setAuthoritiesClaimName("roles");
    roles.setAuthorityPrefix("ROLE_");
    converter.setJwtGrantedAuthoritiesConverter(roles);
    org.springframework.security.web.AuthenticationEntryPoint entry =
        (req, res, e) -> {
          res.setStatus(401);
          res.setContentType("application/json");
          mapper.writeValue(
              res.getOutputStream(),
              new ApiError(
                  "UNAUTHORIZED",
                  "Se requiere autenticación válida",
                  java.time.LocalDateTime.now(),
                  req.getRequestURI()));
        };
    org.springframework.security.web.access.AccessDeniedHandler denied =
        (req, res, e) -> {
          res.setStatus(403);
          res.setContentType("application/json");
          mapper.writeValue(
              res.getOutputStream(),
              new ApiError(
                  "FORBIDDEN",
                  "No tienes permiso para esta operación",
                  java.time.LocalDateTime.now(),
                  req.getRequestURI()));
        };
    return http.csrf(c -> c.disable())
        .cors(org.springframework.security.config.Customizer.withDefaults())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            a ->
                a.requestMatchers(
                        "/api/v1/auth/**",
                        "/actuator/health",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(e -> e.authenticationEntryPoint(entry).accessDeniedHandler(denied))
        .oauth2ResourceServer(
            o ->
                o.jwt(j -> j.jwtAuthenticationConverter(converter))
                    .authenticationEntryPoint(entry)
                    .accessDeniedHandler(denied))
        .build();
  }
}
