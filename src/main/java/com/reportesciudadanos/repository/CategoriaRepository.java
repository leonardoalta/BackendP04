package com.reportesciudadanos.repository;

import com.reportesciudadanos.entity.Categoria;
import org.springframework.data.jpa.repository.*;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
  java.util.List<Categoria> findByActivaTrueOrderByNombreAsc();
}
