package com.reportesciudadanos.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "evidencia")
@Getter
@Setter
public class Evidencia {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Reporte reporte;

  @Column(nullable = false)
  private String tipo;

  @Column(nullable = false)
  private String referenciaFicticia;

  private String descripcion;

  @Column(nullable = false)
  private LocalDateTime fechaRegistro = LocalDateTime.now();
}
