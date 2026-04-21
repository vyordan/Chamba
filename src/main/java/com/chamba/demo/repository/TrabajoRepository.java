package com.chamba.demo.repository;

import com.chamba.demo.model.Trabajo;
import com.chamba.demo.model.Usuario;
import com.chamba.demo.model.enums.EstadoTrabajo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TrabajoRepository extends JpaRepository<Trabajo, Long> {
    List<Trabajo> findByEstado(EstadoTrabajo estado);
    List<Trabajo> findByContratante(Usuario contratante);
}