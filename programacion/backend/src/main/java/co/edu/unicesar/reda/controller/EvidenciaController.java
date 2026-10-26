package co.edu.unicesar.reda.controller;

import co.edu.unicesar.reda.dto.EvidenciaDtos.ArchivoResponse;
import co.edu.unicesar.reda.dto.EvidenciaDtos.EvidenciaResponse;
import co.edu.unicesar.reda.exception.ReglaNegocioException;
import co.edu.unicesar.reda.security.UsuarioAutenticado;
import co.edu.unicesar.reda.service.EvidenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/** Fotos de evidencia. Todas las rutas exigen sesión: las fotos son privadas. */
@RestController
@Tag(name = "Evidencias (fotos privadas)")
@SecurityRequirement(name = "bearer")
public class EvidenciaController {

    private final EvidenciaService evidencias;

    public EvidenciaController(EvidenciaService evidencias) {
        this.evidencias = evidencias;
    }

    @PostMapping(value = "/api/reportes/{id}/evidencias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CIUDADANO')")
    @Operation(summary = "Subir una foto (JPG, PNG o WEBP, máx. 10 MB) a un reporte propio")
    public EvidenciaResponse subir(@PathVariable Integer id, @RequestParam("archivo") MultipartFile archivo,
                                   @AuthenticationPrincipal UsuarioAutenticado usuario) throws IOException {
        if (archivo.isEmpty()) {
            throw new ReglaNegocioException("ARCHIVO_VACIO", "No se recibió ninguna foto");
        }
        return evidencias.subir(id, archivo.getBytes(), usuario.id());
    }

    @GetMapping("/api/reportes/{id}/evidencias")
    @Operation(summary = "Fotos de un reporte (autor o personal de revisión)")
    public List<EvidenciaResponse> listar(@PathVariable Integer id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return evidencias.listar(id, usuario.id());
    }

    @GetMapping("/api/evidencias/{id}/archivo")
    @Operation(summary = "Archivo de la foto (autor o personal de revisión)")
    public ResponseEntity<byte[]> archivo(@PathVariable Integer id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        ArchivoResponse a = evidencias.archivo(id, usuario.id());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(a.tipoMime()))
                // privado: ni el navegador compartido ni un proxy deben guardar una copia
                .cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff")
                .body(a.contenido());
    }
}
