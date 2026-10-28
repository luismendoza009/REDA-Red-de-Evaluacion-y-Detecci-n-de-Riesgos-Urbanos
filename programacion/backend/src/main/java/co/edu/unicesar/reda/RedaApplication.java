package co.edu.unicesar.reda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * Punto de entrada de la API REST de REDA.
 *
 * Arquitectura en capas (cada capa solo conoce a la de abajo):
 *   controller  -> recibe HTTP, valida el formato (peticion/) y delega. No tiene lógica de negocio.
 *   service     -> casos de uso: coordina dominio y repositorios dentro de una transacción.
 *   model       -> entidades del dominio CON comportamiento (reglas de negocio).
 *   repository  -> acceso a datos (Spring Data JPA y SQL nativo parametrizado).
 * Entrada y salida de la API: peticion (formularios validados) y respuesta (JSON devuelto).
 * Transversales: exception (errores), security (JWT), config.
 */
// Se excluye el usuario en memoria que Spring crea por defecto: la autenticación es solo por JWT.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class RedaApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedaApplication.class, args);
    }
}
