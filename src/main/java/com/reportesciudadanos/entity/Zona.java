package com.reportesciudadanos.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "zona")
@Getter
@Setter
public class Zona {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String nombre;

  private String referencia;

  @Column(nullable = false)
  private Boolean activa = true;
}
