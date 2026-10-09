package com.reportesciudadanos.entity;

import com.reportesciudadanos.enums.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "reporte")
@Getter
@Setter
public class Reporte {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Categoria categoria;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Zona zona;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Usuario propietario;

  @Column(nullable = false)
  private String titulo;

  @Column(nullable = false, length = 4000)
  private String descripcion;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EstadoReporte estado = EstadoReporte.REGISTRADO;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Prioridad prioridad;

  @Column(nullable = false)
  private LocalDateTime fechaCreacion = LocalDateTime.now();

  @Column(nullable = false)
  private LocalDateTime fechaActualizacion = LocalDateTime.now();

  @Version private Long version;

  @OneToMany(mappedBy = "reporte", fetch = FetchType.LAZY)
  private java.util.List<com.reportesciudadanos.entity.Evidencia> evidencias =
      new java.util.ArrayList<>();

  @OneToMany(mappedBy = "reporte", fetch = FetchType.LAZY)
  private java.util.List<com.reportesciudadanos.entity.Seguimiento> seguimientos =
      new java.util.ArrayList<>();

  @OneToMany(mappedBy = "reporte", fetch = FetchType.LAZY)
  private java.util.List<com.reportesciudadanos.entity.EventoContexto> eventosContexto =
      new java.util.ArrayList<>();
}
