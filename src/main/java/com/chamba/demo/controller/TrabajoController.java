package com.chamba.demo.controller;

import com.chamba.demo.model.Trabajo;
import com.chamba.demo.model.Usuario;
import com.chamba.demo.service.TrabajoService;
import com.chamba.demo.service.UsuariosService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/trabajos")
public class TrabajoController {

    @Autowired
    private TrabajoService trabajoService;

    @Autowired
    private UsuariosService usuariosService;

    @GetMapping
    public String listarTrabajos(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("usuarioId");
        if (userId == null) return "redirect:/login";
        model.addAttribute("trabajos", trabajoService.listarAbiertos());
        return "trabajos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevoTrabajoForm(Model model, HttpSession session) {
        if (session.getAttribute("usuarioId") == null) return "redirect:/login";
        model.addAttribute("trabajo", new Trabajo());
        return "trabajos/nuevo";
    }

    @PostMapping("/crear")
    public String crearTrabajo(@RequestParam String titulo,
                            @RequestParam String descripcion,
                            @RequestParam String ubicacion,
                            @RequestParam Double monto,
                            HttpSession session,
                            Model model) {
        Long userId = (Long) session.getAttribute("usuarioId");
        Usuario contratante = usuariosService.obtenerPorId(userId);
        try {
            trabajoService.publicar(contratante, titulo, descripcion, ubicacion, monto);
            return "redirect:/trabajos/mis-trabajos";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "trabajos/nuevo";
        }
    }

    @GetMapping("/mis-trabajos")
    public String misTrabajos(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("usuarioId");
        if (userId == null) return "redirect:/login";
        Usuario contratante = usuariosService.obtenerPorId(userId);
        model.addAttribute("trabajos", trabajoService.listarPorContratante(contratante));
        return "trabajos/mis-trabajos";
    }

    @GetMapping("/{id}")
    public String detalleTrabajo(@PathVariable Long id, Model model, HttpSession session) {
        if (session.getAttribute("usuarioId") == null) return "redirect:/login";
        Trabajo trabajo = trabajoService.obtenerPorId(id);
        model.addAttribute("trabajo", trabajo);
        return "trabajos/detalle";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelarTrabajo(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("usuarioId");
        Trabajo trabajo = trabajoService.obtenerPorId(id);
        if (trabajo != null && trabajo.getContratante().getId().equals(userId)) {
            trabajoService.cancelarTrabajo(trabajo);
        }
        return "redirect:/trabajos/mis-trabajos";
    }

}