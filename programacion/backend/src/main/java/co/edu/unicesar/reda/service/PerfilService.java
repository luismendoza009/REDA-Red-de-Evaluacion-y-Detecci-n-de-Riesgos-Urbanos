package co.edu.unicesar.reda.service;

import co.edu.unicesar.reda.dto.AuthDtos.*;
import co.edu.unicesar.reda.exception.ReglaNegocioException;
import co.edu.unicesar.reda.model.Usuario;
import co.edu.unicesar.reda.repository.AbogadoRepository;
import co.edu.unicesar.reda.repository.UsuarioRepository;
import co.edu.unicesar.reda.security.LimitadorIntentos;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Casos de uso: ver y editar mi perfil, cambiar mi contraseña. */
@Service
public class PerfilService {

    private final UsuarioRepository usuarios;
    private final AbogadoRepository abogados;
    private final PasswordEncoder codificador;
    private final LimitadorIntentos limitador;

    public PerfilService(UsuarioRepository usuarios, AbogadoRepository abogados, PasswordEncoder codificador,
                         LimitadorIntentos limitador) {
        this.usuarios = usuarios;
        this.abogados = abogados;
        this.codificador = codificador;
        this.limitador = limitador;
    }

    @Transactional(readOnly = true)
    public PerfilResponse obtener(Integer idUsuario) {
        return perfil(Busqueda.porId(usuarios, idUsuario, "Usuario"));
    }

    @Transactional
    public PerfilResponse actualizar(Integer idUsuario, ActualizarPerfilRequest datos) {
        Usuario u = Busqueda.porId(usuarios, idUsuario, "Usuario");
        u.cambiarNombre(datos.nombre());
        return perfil(u);
    }

    /** Exige la contraseña actual: si alguien deja la sesión abierta, otro no puede cambiarla. */
    @Transactional
    public MensajeResponse cambiarPassword(Integer idUsuario, CambioPasswordRequest datos) {
        limitador.registrar("cambio-password:" + idUsuario);
        Usuario u = Busqueda.porId(usuarios, idUsuario, "Usuario");
        if (!codificador.matches(datos.actual(), u.getPasswordHash())) {
            throw new ReglaNegocioException("PASSWORD_ACTUAL_INCORRECTA", "La contraseña actual no es correcta");
        }
        if (codificador.matches(datos.nueva(), u.getPasswordHash())) {
            throw new ReglaNegocioException("PASSWORD_REPETIDA", "La nueva contraseña debe ser distinta de la actual");
        }
        u.cambiarPassword(codificador.encode(datos.nueva()));
        return new MensajeResponse("Contraseña actualizada.");
    }

    private PerfilResponse perfil(Usuario u) {
        PerfilAbogado abogado = abogados.findByUsuarioId(u.getId())
                .map(a -> new PerfilAbogado(a.getTarjetaProfesional(), a.getEspecialidad(), a.getCiudad(),
                        a.getAniosExperiencia(), a.getPresentacion(), a.isActivo()))
                .orElse(null);
        return new PerfilResponse(u.getId(), u.getNombre(), u.getCorreo(), u.getRol().getNombre().name(),
                u.getFechaRegistro(), abogado);
    }
}
