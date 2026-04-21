package com.chamba.demo.repository;

import com.chamba.demo.model.Resenia;
import com.chamba.demo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReseniasRepository extends JpaRepository<Resenia, Long> {
    List<Resenia> findByDestinatario(Usuario destinatario);
}