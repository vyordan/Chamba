package com.chamba.demo.controller;

import com.chamba.demo.model.Usuario;
import com.chamba.demo.service.UsuariosService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class AuthController {

    @Autowired
    private UsuariosService usuariosService;

    @GetMapping("/")
    public String index(HttpSession session) {
        if (session.getAttribute("usuarioId") != null) {
            return "redirect:/trabajos";
        }
        return "index";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, HttpSession session, Model model) {
        Usuario usuario = usuariosService.login(email, password);
        if (usuario != null) {
            session.setAttribute("usuarioId", usuario.getId());
            session.setAttribute("usuarioNombre", usuario.getNombre());
            session.setAttribute("usuarioTipo", usuario.getTipo());
            return "redirect:/trabajos";
        } else {
            model.addAttribute("error", "Email o contraseña incorrectos");
            return "login";
        }
    }

    @GetMapping("/registro")
    public String registroForm() {
        return "registro";
    }

    @PostMapping("/registro")
    public String registro(@RequestParam String nombre,
                           @RequestParam String email,
                           @RequestParam String telefono,
                           @RequestParam String password,
                           @RequestParam String tipo,
                           @RequestParam String ubicacion,
                           @RequestParam(required = false) MultipartFile foto,
                           Model model) {
        try {
            usuariosService.registrar(nombre, email, telefono, password, tipo, ubicacion, foto);
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "registro";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}