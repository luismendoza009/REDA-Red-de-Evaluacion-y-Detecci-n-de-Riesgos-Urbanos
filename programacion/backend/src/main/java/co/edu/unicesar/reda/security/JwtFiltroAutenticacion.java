package co.edu.unicesar.reda.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lee "Authorization: Bearer <token>" en cada petición y, si el token es válido, deja al usuario
 * autenticado con la autoridad ROLE_<rol>. Si no hay token o es inválido, la petición sigue
 * como anónima y la configuración de seguridad decide si se permite.
 */
@Component
public class JwtFiltroAutenticacion extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;

    public JwtFiltroAutenticacion(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta, FilterChain cadena)
            throws ServletException, IOException {
        String cabecera = peticion.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera != null && cabecera.startsWith(PREFIJO)) {
            jwtService.validar(cabecera.substring(PREFIJO.length())).ifPresent(usuario -> {
                var autoridades = List.of(new SimpleGrantedAuthority("ROLE_" + usuario.rol().name()));
                var autenticacion = new UsernamePasswordAuthenticationToken(usuario, null, autoridades);
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            });
        }
        cadena.doFilter(peticion, respuesta);
    }
}
