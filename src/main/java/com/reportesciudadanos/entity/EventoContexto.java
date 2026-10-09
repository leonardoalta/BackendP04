package com.reportesciudadanos.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "evento_contexto")
@Getter
@Setter
public class EventoContexto {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Reporte reporte;

  @Column(nullable = false)
  private String fuente;

  @Column(nullable = false)
  private LocalDateTime instante = LocalDateTime.now();

  @Column(nullable = false)
  private String variable;

  @Column(nullable = false)
  private String unidad;

  @Column(nullable = false, precision = 19, scale = 6)
  private java.math.BigDecimal valor;

  private String interpretacion;
}
