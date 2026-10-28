package co.edu.unicesar.reda.controller;

import co.edu.unicesar.reda.dto.AuthDtos.*;
import co.edu.unicesar.reda.security.UsuarioAutenticado;
import co.edu.unicesar.reda.service.PerfilService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Mi cuenta: cualquier usuario con sesión, sobre su propia cuenta. */
@RestController
@RequestMapping("/api/cuenta")
@Tag(name = "Mi cuenta")
@SecurityRequirement(name = "bearer")
public class CuentaController {

    private final PerfilService perfil;

    public CuentaController(PerfilService perfil) {
        this.perfil = perfil;
    }

    @GetMapping
    @Operation(summary = "Mi perfil")
    public PerfilResponse obtener(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return perfil.obtener(usuario.id());
    }

    @PatchMapping
    @Operation(summary = "Cambiar mi nombre")
    public PerfilResponse actualizar(@Valid @RequestBody ActualizarPerfilRequest datos,
                                     @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return perfil.actualizar(usuario.id(), datos);
    }

    @PatchMapping("/abogado")
    @PreAuthorize("hasRole('ABOGADO')")
    @Operation(summary = "Editar mi perfil profesional (solo abogados verificados)")
    public PerfilResponse actualizarAbogado(@Valid @RequestBody ActualizarPerfilAbogadoRequest datos,
                                            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return perfil.actualizarAbogado(usuario.id(), datos);
    }

    @PostMapping("/password")
    @Operation(summary = "Cambiar mi contraseña (exige la actual)")
    public MensajeResponse cambiarPassword(@Valid @RequestBody CambioPasswordRequest datos,
                                           @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return perfil.cambiarPassword(usuario.id(), datos);
    }
}
