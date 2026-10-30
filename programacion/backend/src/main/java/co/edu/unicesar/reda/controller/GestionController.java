package co.edu.unicesar.reda.controller;

import co.edu.unicesar.reda.respuesta.AbogadoAdminResponse;
import co.edu.unicesar.reda.respuesta.MensajeResponse;
import co.edu.unicesar.reda.respuesta.ReporteEnRevision;
import co.edu.unicesar.reda.security.LimitadorIntentos;
import co.edu.unicesar.reda.service.AdminService;
import co.edu.unicesar.reda.service.RevisionReporteService;
import co.edu.unicesar.reda.model.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Paneles internos: bandeja del operador y verificación de abogados por el administrador. */
@RestController
@Tag(name = "Gestión interna")
@SecurityRequirement(name = "bearer")
public class GestionController {

    private final RevisionReporteService revision;
    private final AdminService admin;
    private final LimitadorIntentos limitador;

    public GestionController(RevisionReporteService revision, AdminService admin, LimitadorIntentos limitador) {
        this.revision = revision;
        this.admin = admin;
        this.limitador = limitador;
    }

    @GetMapping("/api/revision/bandeja")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
    @Operation(summary = "Reportes que esperan revisión (primero los incidentes)")
    public List<ReporteEnRevision> bandeja() {
        return revision.bandeja();
    }

    @GetMapping("/api/admin/abogados")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Abogados registrados (primero los pendientes de verificación)")
    public List<AbogadoAdminResponse> abogados() {
        return admin.abogados();
    }

    @PostMapping("/api/admin/abogados/{id}/verificacion")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Marcar como verificada la tarjeta profesional de un abogado (lo activa)")
    public AbogadoAdminResponse verificar(@PathVariable Integer id) {
        return admin.activar(id);
    }

    @PostMapping("/api/admin/accesos/desbloqueo")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Desbloquear el acceso de un correo tras verificar la identidad de su titular")
    public MensajeResponse desbloquearAcceso(@RequestParam String correo) {
        limitador.desbloquearLogin("login:" + Usuario.normalizarCorreo(correo));
        return new MensajeResponse("El bloqueo de acceso fue retirado si existía para ese correo.");
    }
}
