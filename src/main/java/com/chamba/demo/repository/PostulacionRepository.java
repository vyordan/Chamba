package com.chamba.demo.repository;

import com.chamba.demo.model.Postulacion;
import com.chamba.demo.model.Trabajo;
import com.chamba.demo.model.Usuario;
import com.chamba.demo.model.enums.EstadoPostulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PostulacionRepository extends JpaRepository<Postulacion, Long> {
    List<Postulacion> findByTrabajo(Trabajo trabajo);
    List<Postulacion> findByTrabajador(Usuario trabajador);
    Optional<Postulacion> findByTrabajoAndTrabajador(Trabajo trabajo, Usuario trabajador);
    List<Postulacion> findByTrabajoAndEstado(Trabajo trabajo, EstadoPostulacion estado);
}