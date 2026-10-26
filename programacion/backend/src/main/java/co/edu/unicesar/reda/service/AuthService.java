package co.edu.unicesar.reda.service;

import co.edu.unicesar.reda.dto.AuthDtos.LoginRequest;
import co.edu.unicesar.reda.dto.AuthDtos.RegistroRequest;
import co.edu.unicesar.reda.dto.AuthDtos.TokenResponse;
import co.edu.unicesar.reda.exception.ConflictoException;
import co.edu.unicesar.reda.exception.CredencialesInvalidasException;
import co.edu.unicesar.reda.model.NombreRol;
import co.edu.unicesar.reda.model.Rol;
import co.edu.unicesar.reda.model.Usuario;
import co.edu.unicesar.reda.repository.RolRepository;
import co.edu.unicesar.reda.repository.UsuarioRepository;
import co.edu.unicesar.reda.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Casos de uso: registrarse como ciudadano e iniciar sesión. */
@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final PasswordEncoder codificador;
    private final JwtService jwt;

    public AuthService(UsuarioRepository usuarios, RolRepository roles, PasswordEncoder codificador, JwtService jwt) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.codificador = codificador;
        this.jwt = jwt;
    }

    /** El registro público siempre crea CIUDADANOS; operadores y abogados los crea un administrador. */
    @Transactional
    public TokenResponse registrar(RegistroRequest datos) {
        String correo = Usuario.normalizarCorreo(datos.correo());
        if (usuarios.existsByCorreo(correo)) {
            throw new ConflictoException("CORREO_REGISTRADO", "Ya existe una cuenta con ese correo");
        }
        Rol ciudadano = roles.findByNombre(NombreRol.CIUDADANO)
                .orElseThrow(() -> new IllegalStateException("Falta el rol CIUDADANO en la base (¿se cargó la semilla?)"));
        Usuario nuevo = usuarios.save(Usuario.registrar(datos.nombre(), correo,
                codificador.encode(datos.password()), ciudadano));
        return emitirToken(nuevo);
    }

    @Transactional(readOnly = true)
    public TokenResponse iniciarSesion(LoginRequest datos) {
        Usuario usuario = usuarios.findByCorreo(Usuario.normalizarCorreo(datos.correo()))
                .filter(Usuario::isActivo)
                .filter(u -> codificador.matches(datos.password(), u.getPasswordHash()))
                .orElseThrow(CredencialesInvalidasException::new);
        return emitirToken(usuario);
    }

    private TokenResponse emitirToken(Usuario u) {
        return new TokenResponse(jwt.generar(u), "Bearer", jwt.getMinutosExpiracion(),
                u.getId(), u.getNombre(), u.getRol().getNombre().name());
    }
}
