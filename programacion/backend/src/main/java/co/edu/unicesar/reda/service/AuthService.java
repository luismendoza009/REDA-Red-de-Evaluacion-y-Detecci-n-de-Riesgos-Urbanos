package co.edu.unicesar.reda.service;

import co.edu.unicesar.reda.dto.AuthDtos.LoginRequest;
import co.edu.unicesar.reda.dto.AuthDtos.RegistroAbogadoRequest;
import co.edu.unicesar.reda.dto.AuthDtos.RegistroRequest;
import co.edu.unicesar.reda.dto.AuthDtos.TokenResponse;
import co.edu.unicesar.reda.exception.ConflictoException;
import co.edu.unicesar.reda.exception.CredencialesInvalidasException;
import co.edu.unicesar.reda.model.Abogado;
import co.edu.unicesar.reda.model.NombreRol;
import co.edu.unicesar.reda.model.Rol;
import co.edu.unicesar.reda.model.Usuario;
import co.edu.unicesar.reda.repository.AbogadoRepository;
import co.edu.unicesar.reda.repository.RolRepository;
import co.edu.unicesar.reda.repository.UsuarioRepository;
import co.edu.unicesar.reda.security.JwtService;
import co.edu.unicesar.reda.security.LimitadorIntentos;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Casos de uso: crear cuenta (ciudadano o abogado) e iniciar sesión. */
@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final AbogadoRepository abogados;
    private final PasswordEncoder codificador;
    private final JwtService jwt;
    private final LimitadorIntentos limitador;

    public AuthService(UsuarioRepository usuarios, RolRepository roles, AbogadoRepository abogados,
                       PasswordEncoder codificador, JwtService jwt, LimitadorIntentos limitador) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.abogados = abogados;
        this.codificador = codificador;
        this.jwt = jwt;
        this.limitador = limitador;
    }

    /** El registro público crea CIUDADANOS; operadores y administradores los crea un administrador. */
    @Transactional
    public TokenResponse registrar(RegistroRequest datos) {
        Usuario nuevo = crearUsuario(datos.nombre(), datos.correo(), datos.password(), NombreRol.CIUDADANO);
        return emitirToken(nuevo);
    }

    /** Un abogado se registra; su cuenta queda pendiente de verificación (no ve casos hasta que la activen). */
    @Transactional
    public TokenResponse registrarAbogado(RegistroAbogadoRequest datos) {
        if (abogados.existsByTarjetaProfesional(datos.tarjetaProfesional())) {
            throw new ConflictoException("TARJETA_REGISTRADA", "Ya existe un abogado con esa tarjeta profesional");
        }
        Usuario usuario = crearUsuario(datos.nombre(), datos.correo(), datos.password(), NombreRol.ABOGADO);
        abogados.save(Abogado.solicitarVinculacion(usuario, datos.tarjetaProfesional(), datos.especialidad(),
                datos.ciudad(), datos.aniosExperiencia(), datos.presentacion()));
        return emitirToken(usuario);
    }

    /** Hasta {@link LimitadorIntentos#MAXIMO} intentos por correo cada 15 minutos. */
    @Transactional(readOnly = true)
    public TokenResponse iniciarSesion(LoginRequest datos) {
        String correo = Usuario.normalizarCorreo(datos.correo());
        String clave = "login:" + correo;
        limitador.registrar(clave);
        Usuario usuario = usuarios.findByCorreo(correo)
                .filter(Usuario::isActivo)
                .filter(u -> codificador.matches(datos.password(), u.getPasswordHash()))
                .orElseThrow(CredencialesInvalidasException::new);
        limitador.reiniciar(clave);
        return emitirToken(usuario);
    }

    private Usuario crearUsuario(String nombre, String correo, String password, NombreRol nombreRol) {
        String normalizado = Usuario.normalizarCorreo(correo);
        if (usuarios.existsByCorreo(normalizado)) {
            throw new ConflictoException("CORREO_REGISTRADO", "Ya existe una cuenta con ese correo");
        }
        Rol rol = roles.findByNombre(nombreRol)
                .orElseThrow(() -> new IllegalStateException("Falta el rol " + nombreRol + " en la base (¿se cargó la semilla?)"));
        return usuarios.save(Usuario.registrar(nombre, normalizado, codificador.encode(password), rol));
    }

    private TokenResponse emitirToken(Usuario u) {
        return new TokenResponse(jwt.generar(u), "Bearer", jwt.getMinutosExpiracion(),
                u.getId(), u.getNombre(), u.getRol().getNombre().name());
    }
}
