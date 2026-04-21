package com.chamba.demo.repository;

import com.chamba.demo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRespository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
}