package com.reportesciudadanos;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reportesciudadanos.entity.Usuario;
import com.reportesciudadanos.enums.RoleEnum;
import com.reportesciudadanos.repository.RolRepository;
import com.reportesciudadanos.repository.UsuarioRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired UsuarioRepository users;
  @Autowired RolRepository roles;
  @Autowired PasswordEncoder encoder;

  @BeforeEach
  void users() {
    for (var role : RoleEnum.values()) {
      String email = role.name().toLowerCase() + "@example.test";
      if (users.findByCorreoElectronico(email).isEmpty()) {
        var u = new Usuario();
        u.setNombre(role.name());
        u.setCorreoElectronico(email);
        u.setPasswordHash(encoder.encode("PruebaSegura123!"));
        u.setRol(roles.findByNombre(role).orElseThrow());
        users.save(u);
      }
    }
    if (users.findByCorreoElectronico("otro@example.test").isEmpty()) {
      var u = new Usuario();
      u.setNombre("Otro");
      u.setCorreoElectronico("otro@example.test");
      u.setPasswordHash(encoder.encode("PruebaSegura123!"));
      u.setRol(roles.findByNombre(RoleEnum.CIUDADANO).orElseThrow());
      users.save(u);
    }
  }

  private org.springframework.test.web.servlet.request.RequestPostProcessor as(RoleEnum r) {
    return jwt()
        .jwt(j -> j.subject(r.name().toLowerCase() + "@example.test"))
        .authorities(new SimpleGrantedAuthority("ROLE_" + r.name()));
  }

  private String body(long categoria, long zona) {
    return "{\"categoriaId\":"
        + categoria
        + ",\"zonaId\":"
        + zona
        + ",\"titulo\":\"Basura\",\"descripcion\":\"Residuos en calle\",\"prioridad\":\"MEDIA\"}";
  }

  private long create() throws Exception {
    var response =
        mvc.perform(
                post("/api/v1/reportes")
                    .with(as(RoleEnum.CIUDADANO))
                    .contentType("application/json")
                    .content(body(1, 1)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("estado").value("REGISTRADO"))
            .andReturn();
    return json.readTree(response.getResponse().getContentAsString()).get("id").asLong();
  }

  private void change(long id, String state, RoleEnum role, int status) throws Exception {
    mvc.perform(
            patch("/api/v1/reportes/" + id + "/estado")
                .with(as(role))
                .contentType("application/json")
                .content("{\"estado\":\"" + state + "\",\"comentario\":\"Cambio de prueba\"}"))
        .andExpect(status().is(status));
  }

  @Test
  void createAndGet() throws Exception {
    long id = create();
    mvc.perform(get("/api/v1/reportes/" + id).with(as(RoleEnum.CIUDADANO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("reporte.id").value(id))
        .andExpect(jsonPath("reporte.passwordHash").doesNotExist());
  }

  @Test
  void missingCategory() throws Exception {
    mvc.perform(
            post("/api/v1/reportes")
                .with(as(RoleEnum.CIUDADANO))
                .contentType("application/json")
                .content(body(9999, 1)))
        .andExpect(status().isNotFound());
  }

  @Test
  void missingZone() throws Exception {
    mvc.perform(
            post("/api/v1/reportes")
                .with(as(RoleEnum.CIUDADANO))
                .contentType("application/json")
                .content(body(1, 9999)))
        .andExpect(status().isNotFound());
  }

  @Test
  void ownership() throws Exception {
    long id = create();
    mvc.perform(
            get("/api/v1/reportes/" + id)
                .with(
                    jwt()
                        .jwt(j -> j.subject("otro@example.test"))
                        .authorities(new SimpleGrantedAuthority("ROLE_CIUDADANO"))))
        .andExpect(status().isForbidden());
    mvc.perform(
            get("/api/v1/reportes/" + id + "/seguimientos")
                .with(
                    jwt()
                        .jwt(j -> j.subject("otro@example.test"))
                        .authorities(new SimpleGrantedAuthority("ROLE_CIUDADANO"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void validChangeRegistersHistory() throws Exception {
    long id = create();
    change(id, "EN_REVISION", RoleEnum.OPERADOR, 200);
    mvc.perform(get("/api/v1/reportes/" + id + "/seguimientos").with(as(RoleEnum.CIUDADANO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].estadoAnterior").value("REGISTRADO"))
        .andExpect(jsonPath("$[0].estadoNuevo").value("EN_REVISION"));
  }

  @Test
  void invalidChangeDoesNotRegisterHistory() throws Exception {
    long id = create();
    change(id, "CERRADO", RoleEnum.OPERADOR, 409);
    mvc.perform(get("/api/v1/reportes/" + id + "/seguimientos").with(as(RoleEnum.CIUDADANO)))
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void unauthenticated() throws Exception {
    mvc.perform(get("/api/v1/reportes"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("code").value("UNAUTHORIZED"));
  }

  @Test
  void unauthorizedRole() throws Exception {
    change(create(), "EN_REVISION", RoleEnum.CIUDADANO, 403);
    mvc.perform(
            post("/api/v1/reportes")
                .with(as(RoleEnum.OPERADOR))
                .contentType("application/json")
                .content(body(1, 1)))
        .andExpect(status().isForbidden());
  }

  @Test
  void roleTransitionRestriction() throws Exception {
    change(create(), "EN_REVISION", RoleEnum.RESPONSABLE_ATENCION, 403);
  }

  @Test
  void publicRegistrationCannotAssignRole() throws Exception {
    mvc.perform(
            post("/api/v1/auth/register")
                .contentType("application/json")
                .content(
                    "{\"nombre\":\"Test\",\"correoElectronico\":\"test@example.test\",\"password\":\"PruebaSegura123!\",\"rol\":\"OPERADOR\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void loginIssuesUsableSignedToken() throws Exception {
    var result =
        mvc.perform(
                post("/api/v1/auth/login")
                    .contentType("application/json")
                    .content(
                        "{\"correoElectronico\":\"ciudadano@example.test\",\"password\":\"PruebaSegura123!\"}"))
            .andExpect(status().isOk())
            .andReturn();
    String token =
        json.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    mvc.perform(get("/api/v1/reportes").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
    mvc.perform(get("/api/v1/reportes").header("Authorization", "Bearer " + token + "x"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void controlledPagination() throws Exception {
    mvc.perform(get("/api/v1/reportes?size=101").with(as(RoleEnum.CIUDADANO)))
        .andExpect(status().isBadRequest());
  }
}
