package com.chamba.demo.controller;

import com.chamba.demo.model.Contrato;
import com.chamba.demo.model.Usuario;
import com.chamba.demo.model.enums.EstadoContrato;
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
        if (userId == null) return "redirect:/login";

        Contrato contrato = contratoRepository.findById(contratoId).orElse(null);
        if (contrato == null || contrato.getEstado() != EstadoContrato.COMPLETADO) {
            return "redirect:/contratos/mis-contratos";
        }

        boolean esContratante = contrato.getContratante().getId().equals(userId);
        boolean esTrabajador  = contrato.getTrabajador() != null && contrato.getTrabajador().getId().equals(userId);

        if (!esContratante && !esTrabajador) {
            return "redirect:/contratos/mis-contratos";
        }

        // Determinar a quién se va a reseñar
        Usuario destinatario = esContratante ? contrato.getTrabajador() : contrato.getContratante();
        model.addAttribute("contrato", contrato);
        model.addAttribute("destinatario", destinatario);
        model.addAttribute("esContratante", esContratante);
        return "resenias/nueva";
    }

    @PostMapping("/crear")
    public String crearResenia(@RequestParam Long contratoId,
                               @RequestParam int puntuacion,
                               @RequestParam String comentario,
                               HttpSession session,
                               Model model) {
        Long userId = (Long) session.getAttribute("usuarioId");
        if (userId == null) return "redirect:/login";

        Usuario autor = usuariosService.obtenerPorId(userId);
        Contrato contrato = contratoRepository.findById(contratoId).orElse(null);
        if (contrato == null || contrato.getEstado() != EstadoContrato.COMPLETADO) {
            return "redirect:/contratos/mis-contratos";
        }

        boolean esContratante = contrato.getContratante().getId().equals(userId);
        boolean esTrabajador  = contrato.getTrabajador() != null && contrato.getTrabajador().getId().equals(userId);

        if (!esContratante && !esTrabajador) {
            return "redirect:/contratos/mis-contratos";
        }

        Usuario destinatario = esContratante ? contrato.getTrabajador() : contrato.getContratante();

        try {
            reseniasService.crearResenia(autor, destinatario, contrato.getTrabajo(), puntuacion, comentario);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("contrato", contrato);
            model.addAttribute("destinatario", destinatario);
            model.addAttribute("esContratante", esContratante);
            return "resenias/nueva";
        }
        return "redirect:/contratos/mis-contratos";
    }
}