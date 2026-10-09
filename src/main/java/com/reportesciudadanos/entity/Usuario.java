package com.reportesciudadanos.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "usuario")
@Getter
@Setter
public class Usuario {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String nombre;

  @Column(nullable = false, unique = true)
  private String correoElectronico;

  @Column(nullable = false)
  private String passwordHash;

  @Column(nullable = false)
  private boolean enabled = true;

  @Column(nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Rol rol;
}
