package co.edu.unicesar.reda.controller;

import co.edu.unicesar.reda.respuesta.NotificacionesResponse;
import co.edu.unicesar.reda.security.UsuarioAutenticado;
import co.edu.unicesar.reda.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notificaciones")
@Tag(name = "Notificaciones")
@SecurityRequirement(name = "bearer")
@PreAuthorize("hasRole('CIUDADANO')")
public class NotificacionController {

    private final NotificacionService notificaciones;

    public NotificacionController(NotificacionService notificaciones) {
        this.notificaciones = notificaciones;
    }

    @GetMapping
    @Operation(summary = "Notificaciones recientes del ciudadano autenticado")
    public NotificacionesResponse listar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return notificaciones.listar(usuario.id());
    }

    @GetMapping("/no-leidas")
    @Operation(summary = "Cantidad de notificaciones no leídas del ciudadano autenticado")
    public long noLeidas(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return notificaciones.noLeidas(usuario.id());
    }

    @PatchMapping("/{id}/leida")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Marcar una notificación propia como leída")
    public void marcarLeida(@PathVariable Integer id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        notificaciones.marcarLeida(id, usuario.id());
    }
}
