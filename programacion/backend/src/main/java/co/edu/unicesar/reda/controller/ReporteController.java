package co.edu.unicesar.reda.controller;

import co.edu.unicesar.reda.dto.ReporteDtos.*;
import co.edu.unicesar.reda.security.UsuarioAutenticado;
import co.edu.unicesar.reda.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador delgado: traduce HTTP <-> casos de uso. No contiene reglas de negocio ni accede a
 * repositorios; solo valida el formato (@Valid), comprueba el rol (@PreAuthorize) y delega.
 */
@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes")
@SecurityRequirement(name = "bearer")
public class ReporteController {

    private final ReporteService reportes;

    public ReporteController(ReporteService reportes) {
        this.reportes = reportes;
    }

    @PostMapping("/condiciones")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CIUDADANO')")
    @Operation(summary = "Reportar una falla sin daño (queda como antecedente)")
    public ReporteDetalleResponse crearCondicion(@Valid @RequestBody FallaRequest datos,
                                                 @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return reportes.crearCondicion(datos, usuario.id());
    }

    @PostMapping("/incidentes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CIUDADANO')")
    @Operation(summary = "Reportar un incidente (hubo daño); devuelve los antecedentes encontrados en el punto")
    public IncidenteCreadoResponse crearIncidente(@Valid @RequestBody CrearIncidenteRequest datos,
                                                  @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return reportes.crearIncidente(datos, usuario.id());
    }

    @GetMapping("/mios")
    @PreAuthorize("hasRole('CIUDADANO')")
    @Operation(summary = "Reportes del ciudadano autenticado")
    public List<ReporteResumenResponse> misReportes(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return reportes.misReportes(usuario.id());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle con línea de tiempo (autor o personal de revisión)")
    public ReporteDetalleResponse detalle(@PathVariable Integer id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return reportes.detalle(id, usuario.id());
    }

    @GetMapping("/{id}/antecedentes")
    @Operation(summary = "Reportes de condición previos en el punto de un incidente (\"la entidad ya sabía\")")
    public List<AntecedenteResponse> antecedentes(@PathVariable Integer id,
                                                  @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return reportes.antecedentes(id, usuario.id());
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
    @Operation(summary = "Cambiar el estado (queda en el historial). Rechazar exige observación")
    public CambioEstadoResponse cambiarEstado(@PathVariable Integer id, @Valid @RequestBody CambioEstadoRequest datos,
                                              @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return reportes.cambiarEstado(id, datos, usuario.id());
    }
}
