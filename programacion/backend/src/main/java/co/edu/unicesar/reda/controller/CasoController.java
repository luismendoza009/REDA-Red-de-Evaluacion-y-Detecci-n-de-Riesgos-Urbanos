package co.edu.unicesar.reda.controller;

import co.edu.unicesar.reda.dto.CasoDtos.*;
import co.edu.unicesar.reda.security.UsuarioAutenticado;
import co.edu.unicesar.reda.service.AbogadoCasosService;
import co.edu.unicesar.reda.service.CasoCiudadanoService;
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
 * Flujo del caso legal:
 *   ciudadano: pide abogado -> ve las postulaciones -> elige
 *   abogado:   ve casos disponibles (anónimos) -> se postula -> si lo eligen, ve el expediente y lo cierra
 */
@RestController
@Tag(name = "Casos y abogados")
@SecurityRequirement(name = "bearer")
public class CasoController {

    private final CasoCiudadanoService ciudadano;
    private final AbogadoCasosService abogado;

    public CasoController(CasoCiudadanoService ciudadano, AbogadoCasosService abogado) {
        this.ciudadano = ciudadano;
        this.abogado = abogado;
    }

    // ------------------------------------------------------------------ ciudadano
    @PostMapping("/api/casos")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CIUDADANO')")
    @Operation(summary = "Buscar abogado para un incidente validado (publica un resumen anónimo)")
    public CasoCiudadanoResponse solicitar(@Valid @RequestBody SolicitarAbogadoRequest datos,
                                           @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ciudadano.solicitarAbogado(datos.idReporte(), datos.autorizaCompartirResumen(), usuario.id());
    }

    @GetMapping("/api/casos/mios")
    @PreAuthorize("hasRole('CIUDADANO')")
    @Operation(summary = "Mis casos y los abogados que se postularon")
    public List<CasoCiudadanoResponse> misCasos(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ciudadano.misCasos(usuario.id());
    }

    @PostMapping("/api/casos/{id}/eleccion")
    @PreAuthorize("hasRole('CIUDADANO')")
    @Operation(summary = "Elegir el abogado que llevará mi caso")
    public CasoCiudadanoResponse elegir(@PathVariable Integer id, @Valid @RequestBody ElegirRequest datos,
                                        @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ciudadano.elegir(id, datos.idPostulacion(), usuario.id());
    }

    // ------------------------------------------------------------------ abogado
    @GetMapping("/api/abogado/casos-disponibles")
    @PreAuthorize("hasRole('ABOGADO')")
    @Operation(summary = "Casos que buscan abogado (resumen anónimo)")
    public List<CasoDisponibleResponse> disponibles(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return abogado.disponibles(usuario.id());
    }

    @PostMapping("/api/abogado/casos/{id}/postulacion")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ABOGADO')")
    @Operation(summary = "Postularme a un caso")
    public CasoDisponibleResponse postular(@PathVariable Integer id, @Valid @RequestBody PostularRequest datos,
                                           @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return abogado.postular(id, datos.mensaje(), usuario.id());
    }

    @DeleteMapping("/api/abogado/casos/{id}/postulacion")
    @PreAuthorize("hasRole('ABOGADO')")
    @Operation(summary = "Retirar mi postulación (mientras el ciudadano no haya decidido)")
    public CasoDisponibleResponse retirar(@PathVariable Integer id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return abogado.retirar(id, usuario.id());
    }

    @GetMapping("/api/abogado/mis-casos")
    @PreAuthorize("hasRole('ABOGADO')")
    @Operation(summary = "Casos donde el ciudadano me eligió (expediente completo)")
    public List<ExpedienteResponse> misCasosAbogado(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return abogado.misCasos(usuario.id());
    }

    @PostMapping("/api/abogado/casos/{id}/cierre")
    @PreAuthorize("hasRole('ABOGADO')")
    @Operation(summary = "Cerrar un caso que llevo")
    public ExpedienteResponse cerrar(@PathVariable Integer id, @Valid @RequestBody CerrarRequest datos,
                                     @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return abogado.cerrar(id, datos.observacion(), usuario.id());
    }
}
