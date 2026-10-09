package com.reportesciudadanos.service;

import com.reportesciudadanos.dto.request.*;
import com.reportesciudadanos.dto.response.*;
import com.reportesciudadanos.dto.response.PageResponse;
import com.reportesciudadanos.entity.Reporte;
import com.reportesciudadanos.enums.*;
import com.reportesciudadanos.enums.RoleEnum;
import com.reportesciudadanos.exception.ApiException;
import com.reportesciudadanos.mapper.ReporteMapper;
import com.reportesciudadanos.repository.ReporteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReporteService {
  private final ReporteRepository repository;
  private final ReporteMapper mapper;
  private final CategoriaService categories;
  private final ZonaService zones;
  private final CurrentUser current;
  private final ReporteAccessService access;
  private final EvidenciaService evidence;
  private final SeguimientoService history;
  private final EventoContextoService events;

  @Transactional
  @PreAuthorize("@currentUser.hasRole('CIUDADANO')")
  public ReporteResponse create(ReporteCreateRequest request) {
    var r = new Reporte();
    r.setCategoria(categories.require(request.categoriaId()));
    r.setZona(zones.require(request.zonaId()));
    r.setPropietario(current.get());
    r.setTitulo(request.titulo().trim());
    r.setDescripcion(request.descripcion());
    r.setPrioridad(request.prioridad());
    return mapper.response(repository.save(r));
  }

  @Transactional(readOnly = true)
  public ReporteDetailResponse get(Long id) {
    var r = access.require(id);
    return new ReporteDetailResponse(
        mapper.response(r), evidence.list(id), history.history(id), events.list(id));
  }

  @Transactional(readOnly = true)
  public PageResponse<ReporteSummaryResponse> list(
      int page,
      int size,
      String sort,
      EstadoReporte estado,
      Prioridad prioridad,
      Long categoria,
      Long zona,
      boolean mine) {
    if (page < 0
        || size < 1
        || size > 100
        || !java.util.Set.of("id", "fechaCreacion", "prioridad", "estado").contains(sort))
      throw new ApiException(400, "INVALID_PAGINATION", "Paginación u ordenamiento inválidos");
    var u = current.get();
    Specification<Reporte> spec =
        (root, q, cb) -> {
          var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
          if (mine || u.getRol().getNombre() == RoleEnum.CIUDADANO)
            predicates.add(cb.equal(root.get("propietario").get("id"), u.getId()));
          if (estado != null) predicates.add(cb.equal(root.get("estado"), estado));
          if (prioridad != null) predicates.add(cb.equal(root.get("prioridad"), prioridad));
          if (categoria != null)
            predicates.add(cb.equal(root.get("categoria").get("id"), categoria));
          if (zona != null) predicates.add(cb.equal(root.get("zona").get("id"), zona));
          return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    return PageResponse.of(
        repository
            .findAll(
                spec,
                PageRequest.of(
                    page,
                    size,
                    Sort.by(Sort.Direction.DESC, sort).and(Sort.by(Sort.Direction.DESC, "id"))))
            .map(mapper::summary));
  }
}
