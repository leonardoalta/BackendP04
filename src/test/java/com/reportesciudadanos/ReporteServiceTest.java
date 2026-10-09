package com.reportesciudadanos;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.reportesciudadanos.dto.request.ReporteCreateRequest;
import com.reportesciudadanos.enums.Prioridad;
import com.reportesciudadanos.exception.ApiException;
import com.reportesciudadanos.mapper.ReporteMapper;
import com.reportesciudadanos.repository.ReporteRepository;
import com.reportesciudadanos.service.*;
import com.reportesciudadanos.service.CategoriaService;
import com.reportesciudadanos.service.CurrentUser;
import com.reportesciudadanos.service.ZonaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {
  @Mock ReporteRepository repository;
  @Mock CategoriaService categories;
  @Mock ZonaService zones;
  @Mock CurrentUser user;

  @Test
  void invalidCategoryNeverPersistsReport() {
    when(categories.require(42L)).thenThrow(ApiException.missing("categoria"));
    var service =
        new ReporteService(
            repository, new ReporteMapper(), categories, zones, user, null, null, null, null);
    assertThrows(
        ApiException.class,
        () ->
            service.create(
                new ReporteCreateRequest(42L, 1L, "Prueba", "Descripción", Prioridad.MEDIA)));
    verifyNoInteractions(repository, zones, user);
  }
}
