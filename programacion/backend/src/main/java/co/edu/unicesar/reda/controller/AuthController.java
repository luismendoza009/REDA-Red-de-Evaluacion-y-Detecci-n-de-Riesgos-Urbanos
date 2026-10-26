package co.edu.unicesar.reda.controller;

import co.edu.unicesar.reda.dto.AuthDtos.LoginRequest;
import co.edu.unicesar.reda.dto.AuthDtos.RegistroRequest;
import co.edu.unicesar.reda.dto.AuthDtos.TokenResponse;
import co.edu.unicesar.reda.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registro de un ciudadano (acepta la política de tratamiento de datos)")
    public TokenResponse registrar(@Valid @RequestBody RegistroRequest datos) {
        return authService.registrar(datos);
    }

    @PostMapping("/login")
    @Operation(summary = "Inicio de sesión; devuelve el token JWT")
    public TokenResponse iniciarSesion(@Valid @RequestBody LoginRequest datos) {
        return authService.iniciarSesion(datos);
    }
}
