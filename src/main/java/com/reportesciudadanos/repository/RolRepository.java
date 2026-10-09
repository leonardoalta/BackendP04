package com.reportesciudadanos.repository;

import com.reportesciudadanos.entity.Rol;
import org.springframework.data.jpa.repository.*;

public interface RolRepository extends JpaRepository<Rol, Long> {
  java.util.Optional<Rol> findByNombre(com.reportesciudadanos.enums.RoleEnum nombre);
}
