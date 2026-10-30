package co.edu.unicesar.reda.security;

import co.edu.unicesar.reda.config.PropiedadesReda;
import co.edu.unicesar.reda.exception.ErrorRespuesta;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Reglas de acceso:
 *  - Sin sesión: registro, login, recuperación de contraseña, la documentación Swagger y los archivos
 *    de la interfaz web (que sin token solo muestra la pantalla de entrada).
 *  - Todo lo demás exige token. El permiso por rol se declara en cada endpoint con @PreAuthorize,
 *    junto al código que protege (así no queda una lista central que se desactualiza).
 * Sin sesión en el servidor (STATELESS) y sin CSRF: la API no usa cookies, usa el token en la cabecera.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain cadenaSeguridad(HttpSecurity http, JwtFiltroAutenticacion filtroJwt,
                                        ObjectMapper json) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(c -> {})
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        // sin sesión SOLO: crear cuenta, entrar y recuperar la contraseña (decisión del grupo:
                        // el mapa y todos los datos exigen iniciar sesión)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // la interfaz web (archivos estáticos); los datos que pide pasan por las reglas de arriba
                        .requestMatchers(HttpMethod.GET, "/", "/index.html", "/css/**", "/js/**", "/img/**",
                                "/icons/**", "/favicon.svg", "/manifest.json").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e.authenticationEntryPoint((peticion, respuesta, ex) -> {
                    // 401 con el mismo formato de error que el resto de la API
                    respuesta.setStatus(401);
                    respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    respuesta.setCharacterEncoding("UTF-8");
                    json.writeValue(respuesta.getOutputStream(), ErrorRespuesta.de(401, "NO_AUTENTICADO",
                            "Debe iniciar sesión para usar este recurso", peticion.getRequestURI()));
                }))
                .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    PasswordEncoder codificadorContrasenas() {
        return new BCryptPasswordEncoder(10);
    }

    /** CORS restrictivo: solo los orígenes configurados (el frontend), no "*". */
    @Bean
    CorsConfigurationSource configuracionCors(PropiedadesReda propiedades) {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(propiedades.cors().origenes());
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/**", c);
        return fuente;
    }
}
