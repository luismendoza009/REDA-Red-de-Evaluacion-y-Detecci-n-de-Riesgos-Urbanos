package co.edu.unicesar.reda.security;

import co.edu.unicesar.reda.exception.DemasiadosIntentosException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limita los intentos por clave (p. ej. "login:correo" o "recuperar:ip") en una ventana de tiempo.
 * Frena ataques de fuerza bruta contra contraseñas y el abuso del correo de recuperación.
 * Vive en memoria: suficiente para un servidor; con varios servidores iría a Redis o a la base.
 */
@Component
public class LimitadorIntentos {

    public static final int MAXIMO = 8;
    public static final Duration VENTANA = Duration.ofMinutes(15);

    private final Map<String, Deque<Instant>> intentos = new ConcurrentHashMap<>();
    private final Clock reloj;

    public LimitadorIntentos() {
        this(Clock.systemUTC());
    }

    LimitadorIntentos(Clock reloj) {
        this.reloj = reloj;
    }

    /** Registra un intento; lanza 429 si la clave superó el máximo dentro de la ventana. */
    public void registrar(String clave) {
        Instant ahora = reloj.instant();
        Deque<Instant> lista = intentos.computeIfAbsent(clave, k -> new ArrayDeque<>());
        synchronized (lista) {
            while (!lista.isEmpty() && lista.peekFirst().isBefore(ahora.minus(VENTANA))) {
                lista.pollFirst();
            }
            if (lista.size() >= MAXIMO) {
                throw new DemasiadosIntentosException(VENTANA.toMinutes());
            }
            lista.addLast(ahora);
        }
    }

    /** Un inicio de sesión correcto limpia el contador de esa clave. */
    public void reiniciar(String clave) {
        intentos.remove(clave);
    }
}
