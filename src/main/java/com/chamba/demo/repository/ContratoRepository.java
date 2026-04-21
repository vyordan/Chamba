package com.chamba.demo.repository;

import com.chamba.demo.model.Contrato;
import com.chamba.demo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {
    List<Contrato> findByContratante(Usuario contratante);
    List<Contrato> findByTrabajador(Usuario trabajador);
}