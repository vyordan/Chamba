package com.chamba.demo.controller;

import com.chamba.demo.model.*;
import com.chamba.demo.model.enums.EstadoContrato;
import com.chamba.demo.repository.ContratoRepository;  // ← importar
import com.chamba.demo.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/postulaciones")
public class PostulacionController {

    @Autowired
    private PostulacionService postulacionService;

    @Autowired
    private TrabajoService trabajoService;

    @Autowired
    private UsuariosService usuariosService;

    // @Autowired private PagoService pagoService;  // si no lo usas, coméntalo o elimínalo

    @Autowired
    private ContratoRepository contratoRepository;  // ← AGREGAR ESTO

    @PostMapping("/crear")
    public String postular(@RequestParam Long trabajoId, @RequestParam String mensaje, HttpSession session) {
        Long userId = (Long) session.getAttribute("usuarioId");
        Usuario trabajador = usuariosService.obtenerPorId(userId);
        Trabajo trabajo = trabajoService.obtenerPorId(trabajoId);
        postulacionService.postular(trabajador, trabajo, mensaje);
        return "redirect:/trabajos/" + trabajoId;
    }

    @GetMapping("/recibidas")
    public String postulacionesRecibidas(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("usuarioId");
        Usuario contratante = usuariosService.obtenerPorId(userId);
        model.addAttribute("trabajos", trabajoService.listarPorContratante(contratante));
        return "postulaciones/recibidas";
    }

    @PostMapping("/{id}/aceptar")
    public String aceptarPostulacion(@PathVariable Long id, HttpSession session) {
        Postulacion post = postulacionService.obtenerPorId(id);
        Trabajo trabajo = post.getTrabajo();
        Usuario contratante = trabajo.getContratante();
        Usuario trabajador = post.getTrabajador();

        Long userId = (Long) session.getAttribute("usuarioId");
        if (!contratante.getId().equals(userId)) {
            return "redirect:/postulaciones/recibidas";
        }

        // Buscar el contrato asociado al trabajo
        Contrato contrato = contratoRepository.findByTrabajo(trabajo)
            .orElseThrow(() -> new RuntimeException("Contrato no encontrado"));
        
        if (contrato.getEstado() != EstadoContrato.PENDIENTE_ASIGNACION) {
            throw new RuntimeException("El trabajo ya no está disponible para asignar");
        }
        
        // Asignar el trabajador y cambiar estado
        contrato.setTrabajador(trabajador);
        contrato.setEstado(EstadoContrato.EN_PROGRESO);
        contratoRepository.save(contrato);
        
        // Aceptar la postulación y cerrar el trabajo
        postulacionService.aceptarPostulacion(post);
        trabajoService.cerrarTrabajo(trabajo);
        
        return "redirect:/contratos/mis-contratos";
    }

    @PostMapping("/{id}/rechazar")
    public String rechazarPostulacion(@PathVariable Long id, HttpSession session) {
        Postulacion post = postulacionService.obtenerPorId(id);
        Long userId = (Long) session.getAttribute("usuarioId");
        if (post.getTrabajo().getContratante().getId().equals(userId)) {
            postulacionService.rechazarPostulacion(post);
        }
        return "redirect:/postulaciones/recibidas";
    }

    @GetMapping("/mis-postulaciones")
    public String misPostulaciones(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("usuarioId");
        Usuario trabajador = usuariosService.obtenerPorId(userId);
        model.addAttribute("postulaciones", postulacionService.listarPorTrabajador(trabajador));
        return "postulaciones/mis-postulaciones";
    }
}