package co.edu.unicesar.reda.security;

import co.edu.unicesar.reda.exception.DemasiadosIntentosException;
import co.edu.unicesar.reda.model.BloqueoLogin;
import co.edu.unicesar.reda.repository.BloqueoLoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limita los intentos por clave (p. ej. "login:correo" o "recuperar:ip") en una ventana de tiempo.
 * Frena ataques de fuerza bruta contra contraseñas y el abuso del correo de recuperación.
 * Los límites generales viven en memoria y el estado progresivo de inicio de sesión queda en PostgreSQL.
 */
@Component
public class LimitadorIntentos {

    public static final int MAXIMO = 8;
    public static final Duration VENTANA = Duration.ofMinutes(15);
    public static final int INTENTOS_POR_ETAPA_LOGIN = 5;
    public static final long BLOQUEO_LOGIN_INICIAL_DIAS = 1;
    public static final long BLOQUEO_LOGIN_SEGUNDO_DIAS = 2;
    public static final int MAXIMO_INTENTOS_LOGIN = INTENTOS_POR_ETAPA_LOGIN * 3;
    private static final ZoneId ZONA_REDA = ZoneId.of("America/Bogota");

    private final Map<String, Deque<Instant>> intentos = new ConcurrentHashMap<>();
    private final Map<String, ProgresoLogin> intentosLogin = new ConcurrentHashMap<>();
    private final Clock reloj;
    private final BloqueoLoginRepository bloqueosLogin;

    @Autowired
    public LimitadorIntentos(BloqueoLoginRepository bloqueosLogin) {
        this(Clock.systemUTC(), bloqueosLogin);
    }

    LimitadorIntentos(Clock reloj) {
        this(reloj, null);
    }

    private LimitadorIntentos(Clock reloj, BloqueoLoginRepository bloqueosLogin) {
        this.reloj = reloj;
        this.bloqueosLogin = bloqueosLogin;
    }

    /** Rechaza el acceso mientras el correo esté en una pausa o bloqueado permanentemente. */
    public void comprobarLoginPermitido(String clave) {
        ProgresoLogin progreso = intentosLogin.computeIfAbsent(clave, this::cargarProgreso);
        synchronized (progreso) {
            comprobarBloqueo(progreso);
        }
    }

    /** Registra solo credenciales incorrectas y activa una pausa al completar cada etapa. */
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = DemasiadosIntentosException.class)
    public int registrarFalloLogin(String clave) {
        ProgresoLogin progreso = intentosLogin.computeIfAbsent(clave, k -> new ProgresoLogin());
        synchronized (progreso) {
            comprobarBloqueo(progreso);
            progreso.fallos++;
            if (progreso.fallos >= MAXIMO_INTENTOS_LOGIN) {
                progreso.bloqueadoPermanentemente = true;
                persistir(clave, progreso);
                throw DemasiadosIntentosException.permanente();
            }
            if (progreso.fallos == INTENTOS_POR_ETAPA_LOGIN) {
                progreso.bloqueadoHasta = fechaHoy().plusDays(BLOQUEO_LOGIN_INICIAL_DIAS);
                persistir(clave, progreso);
                throw DemasiadosIntentosException.temporal(segundosRestantes(progreso.bloqueadoHasta),
                        intentosHastaSiguienteEtapa(progreso));
            }
            if (progreso.fallos == INTENTOS_POR_ETAPA_LOGIN * 2) {
                progreso.bloqueadoHasta = fechaHoy().plusDays(BLOQUEO_LOGIN_SEGUNDO_DIAS);
                persistir(clave, progreso);
                throw DemasiadosIntentosException.temporal(segundosRestantes(progreso.bloqueadoHasta),
                        intentosHastaSiguienteEtapa(progreso));
            }
            persistir(clave, progreso);
            return intentosHastaSiguienteEtapa(progreso);
        }
    }

    /** Elimina las pausas y el bloqueo permanente tras la intervención del administrador. */
    public void desbloquearLogin(String clave) {
        intentosLogin.remove(clave);
        if (bloqueosLogin != null) {
            bloqueosLogin.deleteById(correoDe(clave));
        }
    }

    private void comprobarBloqueo(ProgresoLogin progreso) {
        if (progreso.bloqueadoPermanentemente) {
            throw DemasiadosIntentosException.permanente();
        }
        if (progreso.bloqueadoHasta != null && !fechaHoy().isAfter(progreso.bloqueadoHasta)) {
            throw DemasiadosIntentosException.temporal(segundosRestantes(progreso.bloqueadoHasta),
                    intentosHastaSiguienteEtapa(progreso));
        }
    }

    private LocalDate fechaHoy() {
        return LocalDate.ofInstant(reloj.instant(), ZONA_REDA);
    }

    private long segundosRestantes(LocalDate ultimoDiaBloqueado) {
        Instant finBloqueo = ultimoDiaBloqueado.plusDays(1).atStartOfDay(ZONA_REDA).toInstant();
        return Math.max(1, Duration.between(reloj.instant(), finBloqueo).toSeconds());
    }

    private static int intentosHastaSiguienteEtapa(ProgresoLogin progreso) {
        if (progreso.bloqueadoPermanentemente) return 0;
        return INTENTOS_POR_ETAPA_LOGIN - progreso.fallos % INTENTOS_POR_ETAPA_LOGIN;
    }

    private ProgresoLogin cargarProgreso(String clave) {
        if (bloqueosLogin == null) return new ProgresoLogin();
        return bloqueosLogin.findById(correoDe(clave))
                .map(b -> new ProgresoLogin(b.getIntentosFallidos(), b.getBloqueadoHasta(), b.isBloqueadoPermanente()))
                .orElseGet(ProgresoLogin::new);
    }

    private void persistir(String clave, ProgresoLogin progreso) {
        if (bloqueosLogin != null) {
            bloqueosLogin.save(new BloqueoLogin(correoDe(clave), progreso.fallos, progreso.bloqueadoHasta,
                    progreso.bloqueadoPermanentemente));
        }
    }

    private static String correoDe(String clave) {
        return clave.startsWith("login:") ? clave.substring("login:".length()) : clave;
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

    /** Un inicio de sesión correcto limpia el contador persistente de esa clave. */
    public void reiniciar(String clave) {
        intentos.remove(clave);
        intentosLogin.remove(clave);
        if (bloqueosLogin != null) {
            bloqueosLogin.deleteById(correoDe(clave));
        }
    }

    private static final class ProgresoLogin {
        private int fallos;
        private LocalDate bloqueadoHasta;
        private boolean bloqueadoPermanentemente;

        private ProgresoLogin() {
            this(0, null, false);
        }

        private ProgresoLogin(int fallos, LocalDate bloqueadoHasta, boolean bloqueadoPermanentemente) {
            this.fallos = fallos;
            this.bloqueadoHasta = bloqueadoHasta;
            this.bloqueadoPermanentemente = bloqueadoPermanentemente;
        }
    }
}
