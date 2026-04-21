package com.chamba.demo.controller;

import com.chamba.demo.model.Contrato;
import com.chamba.demo.model.Usuario;
import com.chamba.demo.repository.ContratoRepository;
import com.chamba.demo.service.ReseniasService;
import com.chamba.demo.service.UsuariosService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/resenias")
public class ReseniasController {

    @Autowired
    private ReseniasService reseniasService;

    @Autowired
    private UsuariosService usuariosService;

    @Autowired
    private ContratoRepository contratoRepository;

    @GetMapping("/nueva/{contratoId}")
    public String formResenia(@PathVariable Long contratoId, Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("usuarioId");
        Contrato contrato = contratoRepository.findById(contratoId).orElse(null);
        if (contrato == null || !contrato.getContratante().getId().equals(userId)) {
            return "redirect:/contratos/mis-contratos";
        }
        model.addAttribute("contrato", contrato);
        return "resenias/nueva";
    }

    @PostMapping("/crear")
    public String crearResenia(@RequestParam Long contratoId,
                               @RequestParam int puntuacion,
                               @RequestParam String comentario,
                               HttpSession session) {
        Long userId = (Long) session.getAttribute("usuarioId");
        Usuario autor = usuariosService.obtenerPorId(userId);
        Contrato contrato = contratoRepository.findById(contratoId).orElse(null);
        if (contrato != null && contrato.getContratante().getId().equals(userId)) {
            reseniasService.crearResenia(autor, contrato.getTrabajador(), contrato.getTrabajo(), puntuacion, comentario);
        }
        return "redirect:/contratos/mis-contratos";
    }
}