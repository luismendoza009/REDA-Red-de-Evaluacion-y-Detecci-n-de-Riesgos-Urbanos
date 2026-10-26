package co.edu.unicesar.reda.controller;

import co.edu.unicesar.reda.dto.AuthDtos.*;
import co.edu.unicesar.reda.service.AuthService;
import co.edu.unicesar.reda.service.RecuperacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** Endpoints sin sesión: crear cuenta, entrar y recuperar la contraseña. */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación")
public class AuthController {

    private final AuthService authService;
    private final RecuperacionService recuperacion;

    public AuthController(AuthService authService, RecuperacionService recuperacion) {
        this.authService = authService;
        this.recuperacion = recuperacion;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear cuenta de ciudadano (acepta términos y tratamiento de datos)")
    public TokenResponse registrar(@Valid @RequestBody RegistroRequest datos) {
        return authService.registrar(datos);
    }

    @PostMapping("/registro-abogado")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Solicitar cuenta de abogado (queda pendiente de verificación de la tarjeta profesional)")
    public TokenResponse registrarAbogado(@Valid @RequestBody RegistroAbogadoRequest datos) {
        return authService.registrarAbogado(datos);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión; devuelve el token JWT (máx. 8 intentos cada 15 min por correo)")
    public TokenResponse iniciarSesion(@Valid @RequestBody LoginRequest datos) {
        return authService.iniciarSesion(datos);
    }

    @PostMapping("/recuperar")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Pedir enlace para restablecer la contraseña (la respuesta es la misma exista o no el correo)")
    public MensajeResponse recuperar(@Valid @RequestBody RecuperarRequest datos) {
        return recuperacion.solicitar(datos.correo());
    }

    @PostMapping("/restablecer")
    @Operation(summary = "Restablecer la contraseña con el enlace recibido (un solo uso, vence en 30 minutos)")
    public MensajeResponse restablecer(@Valid @RequestBody RestablecerRequest datos) {
        return recuperacion.restablecer(datos.token(), datos.nuevaPassword());
    }
}
