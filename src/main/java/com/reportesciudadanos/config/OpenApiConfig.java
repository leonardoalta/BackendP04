package com.reportesciudadanos.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfig {
  @Bean
  OpenAPI api() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Reportes Ciudadanos")
                .version("v1")
                .description(
                    "API REST. Registro público exclusivo de ciudadanos. Evidencias ficticias y"
                        + " eventos simulados."))
        .components(
            new Components()
                .addSecuritySchemes(
                    "bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
  }

  @Bean
  org.springdoc.core.customizers.OpenApiCustomizer responses() {
    return api ->
        api.getPaths()
            .forEach(
                (path, item) ->
                    item.readOperationsMap()
                        .forEach(
                            (method, operation) -> {
                              if (path.startsWith("/api/v1/auth/"))
                                operation.setSecurity(java.util.List.of());
                              if (method == io.swagger.v3.oas.models.PathItem.HttpMethod.POST
                                  && !path.endsWith("/login")) {
                                var success = operation.getResponses().remove("200");
                                if (success != null)
                                  operation
                                      .getResponses()
                                      .addApiResponse("201", success.description("Recurso creado"));
                              }
                              java.util.Map.of(
                                      "400",
                                      "Datos inválidos",
                                      "401",
                                      "Autenticación requerida",
                                      "403",
                                      "Acceso prohibido",
                                      "404",
                                      "Recurso inexistente",
                                      "409",
                                      "Conflicto o transición inválida",
                                      "500",
                                      "Error inesperado")
                                  .forEach(
                                      (code, description) ->
                                          operation
                                              .getResponses()
                                              .addApiResponse(
                                                  code,
                                                  new io.swagger.v3.oas.models.responses
                                                          .ApiResponse()
                                                      .description(description)
                                                      .content(
                                                          new io.swagger.v3.oas.models.media
                                                                  .Content()
                                                              .addMediaType(
                                                                  "application/json",
                                                                  new io.swagger.v3.oas.models.media
                                                                          .MediaType()
                                                                      .schema(
                                                                          new io.swagger.v3.oas
                                                                                  .models.media
                                                                                  .ObjectSchema()
                                                                              .addProperty(
                                                                                  "code",
                                                                                  new io.swagger.v3
                                                                                      .oas.models
                                                                                      .media
                                                                                      .StringSchema())
                                                                              .addProperty(
                                                                                  "message",
                                                                                  new io.swagger.v3
                                                                                      .oas.models
                                                                                      .media
                                                                                      .StringSchema())
                                                                              .addProperty(
                                                                                  "timestamp",
                                                                                  new io.swagger.v3
                                                                                          .oas
                                                                                          .models
                                                                                          .media
                                                                                          .StringSchema()
                                                                                      .format(
                                                                                          "date-time"))
                                                                              .addProperty(
                                                                                  "path",
                                                                                  new io.swagger.v3
                                                                                      .oas.models
                                                                                      .media
                                                                                      .StringSchema()))))));
                            }));
  }
}
