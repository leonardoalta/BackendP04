package com.reportesciudadanos.repository;

import com.reportesciudadanos.entity.Usuario;
import org.springframework.data.jpa.repository.*;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
  @EntityGraph(attributePaths = "rol")
  java.util.Optional<Usuario> findByCorreoElectronico(String correoElectronico);
}
