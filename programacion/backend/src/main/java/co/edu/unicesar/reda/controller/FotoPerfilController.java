package co.edu.unicesar.reda.controller;

import co.edu.unicesar.reda.dto.EvidenciaDtos.ArchivoResponse;
import co.edu.unicesar.reda.security.UsuarioAutenticado;
import co.edu.unicesar.reda.service.FotoPerfilService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Foto de perfil. Todo exige sesión; quién ve cada foto lo decide Usuario.fotoVisiblePara. */
@RestController
@Tag(name = "Mi cuenta")
@SecurityRequirement(name = "bearer")
public class FotoPerfilController {

    private final FotoPerfilService fotos;

    public FotoPerfilController(FotoPerfilService fotos) {
        this.fotos = fotos;
    }

    @PutMapping(value = "/api/cuenta/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cambiar mi foto de perfil (JPG, PNG o WEBP, máx. 5 MB; abogados: solo verificados)")
    public Map<String, String> cambiar(@RequestParam("archivo") MultipartFile archivo,
                                       @AuthenticationPrincipal UsuarioAutenticado usuario) throws IOException {
        return Map.of("foto", fotos.cambiar(usuario.id(), archivo.getBytes()));
    }

    @DeleteMapping("/api/cuenta/foto")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Quitar mi foto de perfil")
    public void quitar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        fotos.quitar(usuario.id());
    }

    @GetMapping("/api/usuarios/{id}/foto")
    @Operation(summary = "Foto de perfil de un usuario (la propia, o la de un abogado verificado)")
    public ResponseEntity<byte[]> ver(@PathVariable Integer id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        ArchivoResponse a = fotos.ver(id, usuario.id());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(a.tipoMime()))
                // la ruta lleva la versión (?v=): se puede guardar en el navegador de quien la ve, nunca en un proxy
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePrivate())
                .header("X-Content-Type-Options", "nosniff")
                .body(a.contenido());
    }
}
