package co.edu.unicesar.reda.service;

import co.edu.unicesar.reda.dto.AuthDtos.MensajeResponse;
import co.edu.unicesar.reda.exception.ReglaNegocioException;
import co.edu.unicesar.reda.model.TokenRecuperacion;
import co.edu.unicesar.reda.model.Usuario;
import co.edu.unicesar.reda.repository.TokenRecuperacionRepository;
import co.edu.unicesar.reda.repository.UsuarioRepository;
import co.edu.unicesar.reda.security.LimitadorIntentos;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;

/**
 * Recuperación de contraseña con enlace de un solo uso.
 * Medidas: el token es aleatorio criptográfico (256 bits), en la base solo se guarda su hash, vence a los
 * 30 minutos, sirve una sola vez, pedir uno nuevo anula los anteriores y la respuesta es SIEMPRE la misma
 * (exista o no el correo), para no revelar quién tiene cuenta.
 */
@Service
public class RecuperacionService {

    static final MensajeResponse RESPUESTA_GENERICA = new MensajeResponse(
            "Si el correo está registrado, te enviamos un enlace para restablecer la contraseña. Vence en 30 minutos.");

    private final UsuarioRepository usuarios;
    private final TokenRecuperacionRepository tokens;
    private final PasswordEncoder codificador;
    private final EnvioCorreo correo;
    private final LimitadorIntentos limitador;
    private final String urlBase;
    private final SecureRandom aleatorio = new SecureRandom();

    public RecuperacionService(UsuarioRepository usuarios, TokenRecuperacionRepository tokens, PasswordEncoder codificador,
                               EnvioCorreo correo, LimitadorIntentos limitador,
                               @Value("${reda.correo.url-base}") String urlBase) {
        this.usuarios = usuarios;
        this.tokens = tokens;
        this.codificador = codificador;
        this.correo = correo;
        this.limitador = limitador;
        this.urlBase = urlBase;
    }

    @Transactional
    public MensajeResponse solicitar(String correoIngresado) {
        String normalizado = Usuario.normalizarCorreo(correoIngresado);
        limitador.registrar("recuperar:" + normalizado);
        usuarios.findByCorreo(normalizado).filter(Usuario::isActivo).ifPresent(this::enviarEnlace);
        return RESPUESTA_GENERICA;
    }

    @Transactional
    public MensajeResponse restablecer(String token, String nuevaPassword) {
        OffsetDateTime ahora = OffsetDateTime.now();
        TokenRecuperacion registro = tokens.findByHashToken(EvidenciaService.sha256(token.getBytes(StandardCharsets.UTF_8)))
                .filter(t -> t.sirveEn(ahora))
                .orElseThrow(() -> new ReglaNegocioException("ENLACE_INVALIDO",
                        "El enlace no es válido o ya venció. Pide uno nuevo."));
        registro.getUsuario().cambiarPassword(codificador.encode(nuevaPassword));
        registro.usar(ahora);
        return new MensajeResponse("Listo. Ya puedes iniciar sesión con tu nueva contraseña.");
    }

    private void enviarEnlace(Usuario usuario) {
        byte[] bytes = new byte[32];
        aleatorio.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokens.borrarPendientesDe(usuario.getId());
        tokens.save(TokenRecuperacion.para(usuario, EvidenciaService.sha256(token.getBytes(StandardCharsets.UTF_8))));
        correo.enviar(usuario.getCorreo(), "REDA · Restablecer contraseña", """
                Hola, %s:

                Recibimos una solicitud para restablecer tu contraseña. Abre este enlace (vence en 30 minutos):
                %s/#/restablecer/%s

                Si no fuiste tú, ignora este mensaje: tu contraseña no cambiará.
                """.formatted(usuario.getNombre(), urlBase, token));
    }
}
