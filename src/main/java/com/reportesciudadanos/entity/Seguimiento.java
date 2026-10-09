package com.reportesciudadanos.entity;

import com.reportesciudadanos.enums.EstadoReporte;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "seguimiento")
@Getter
@Setter
public class Seguimiento {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Reporte reporte;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EstadoReporte estadoAnterior;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EstadoReporte estadoNuevo;

  @Column(nullable = false, length = 2000)
  private String comentario;

  @Column(nullable = false)
  private String actorRol;

  @Column(nullable = false)
  private LocalDateTime fecha = LocalDateTime.now();
}
