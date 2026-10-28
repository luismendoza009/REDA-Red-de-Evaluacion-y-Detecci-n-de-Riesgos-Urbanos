package co.edu.unicesar.reda.security;

import co.edu.unicesar.reda.config.PropiedadesReda;
import co.edu.unicesar.reda.model.NombreRol;
import co.edu.unicesar.reda.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

/**
 * Emite y valida tokens JWT firmados con HMAC-SHA (jjwt elige HS256, HS384 o HS512 según la
 * longitud del secreto; con 48 caracteres usa HS384).
 * GRASP Fabricación pura: la responsabilidad "tokens" no pertenece a ninguna entidad del negocio.
 */
@Service
public class JwtService {

    private final SecretKey clave;
    private final long minutosExpiracion;

    public JwtService(PropiedadesReda propiedades) {
        this.clave = Keys.hmacShaKeyFor(propiedades.jwt().secreto().getBytes(StandardCharsets.UTF_8));
        this.minutosExpiracion = propiedades.jwt().minutosExpiracion();
    }

    public String generar(Usuario usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(usuario.getId()))
                .claim("nombre", usuario.getNombre())
                .claim("rol", usuario.getRol().getNombre().name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(minutosExpiracion, ChronoUnit.MINUTES)))
                .signWith(clave)
                .compact();
    }

    /** Devuelve vacío si el token es inválido, está vencido o fue alterado (nunca lanza al filtro). */
    public Optional<UsuarioAutenticado> validar(String token) {
        try {
            Claims c = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
            return Optional.of(new UsuarioAutenticado(
                    Integer.valueOf(c.getSubject()),
                    c.get("nombre", String.class),
                    NombreRol.valueOf(c.get("rol", String.class))));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public long getMinutosExpiracion() {
        return minutosExpiracion;
    }
}
