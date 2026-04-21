package com.chamba.demo.service;

import com.chamba.demo.model.Usuario;
import com.chamba.demo.model.Wallet;
import com.chamba.demo.repository.UsuarioRespository;
import com.chamba.demo.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UsuariosService {

    @Autowired
    private UsuarioRespository usuarioRepository;

    @Autowired
    private WalletRepository walletRepository;

    private final String uploadDir = "./src/main/resources/static/uploads/";

    public Usuario registrar(String nombre, String email, String telefono, String password, String tipo, String ubicacion, MultipartFile foto) throws IOException {
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setTelefono(telefono);
        usuario.setPassword(password);
        usuario.setTipo(tipo);
        usuario.setUbicacion(ubicacion);
        usuario.setFechaRegistro(LocalDateTime.now());

        if (foto != null && !foto.isEmpty()) {
            String nombreArchivo = UUID.randomUUID().toString() + "_" + foto.getOriginalFilename();
            Path ruta = Paths.get(uploadDir + nombreArchivo);
            Files.createDirectories(ruta.getParent());
            Files.write(ruta, foto.getBytes());
            usuario.setFotoUrl("/uploads/" + nombreArchivo);
        }

        Usuario saved = usuarioRepository.save(usuario);
        Wallet wallet = new Wallet();
        wallet.setSaldo(0.0);
        wallet.setUsuario(saved);
        walletRepository.save(wallet);
        saved.setWallet(wallet);
        return saved;
    }

    public Usuario login(String email, String password) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isPresent() && usuarioOpt.get().getPassword().equals(password)) {
            return usuarioOpt.get();
        }
        return null;
    }

    public Usuario obtenerPorId(@NonNull Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }
}