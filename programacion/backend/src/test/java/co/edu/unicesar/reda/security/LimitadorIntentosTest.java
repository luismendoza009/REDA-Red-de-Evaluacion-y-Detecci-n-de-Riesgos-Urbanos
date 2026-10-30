package co.edu.unicesar.reda.security;

import co.edu.unicesar.reda.exception.DemasiadosIntentosException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LimitadorIntentosTest {

    /** Reloj que se puede adelantar a mano, para probar la ventana de tiempo sin esperar 15 minutos. */
    static class RelojManual extends Clock {
        Instant ahora = Instant.parse("2026-10-25T12:00:00Z");
        @Override public java.time.ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zona) { return this; }
        @Override public Instant instant() { return ahora; }
    }

    @Test
    void bloqueaAlSuperarElMaximoYDesbloqueaAlPasarLaVentana() {
        RelojManual reloj = new RelojManual();
        LimitadorIntentos limitador = new LimitadorIntentos(reloj);
        for (int i = 0; i < LimitadorIntentos.MAXIMO; i++) {
            limitador.registrar("login:ana@correo.test");
        }
        assertThrows(DemasiadosIntentosException.class, () -> limitador.registrar("login:ana@correo.test"));
        assertDoesNotThrow(() -> limitador.registrar("login:otra@correo.test"), "cada clave lleva su propia cuenta");

        reloj.ahora = reloj.ahora.plus(LimitadorIntentos.VENTANA).plusSeconds(1);
        assertDoesNotThrow(() -> limitador.registrar("login:ana@correo.test"), "pasada la ventana se puede volver a intentar");
    }

    @Test
    void unAccesoCorrectoReiniciaElContador() {
        LimitadorIntentos limitador = new LimitadorIntentos(new RelojManual());
        for (int i = 0; i < LimitadorIntentos.MAXIMO; i++) {
            limitador.registrar("login:ana@correo.test");
        }
        limitador.reiniciar("login:ana@correo.test");
        assertDoesNotThrow(() -> limitador.registrar("login:ana@correo.test"));
    }

    @Test
    void aplicaPausasProgresivasYBloqueoAdministrativo() {
        RelojManual reloj = new RelojManual();
        LimitadorIntentos limitador = new LimitadorIntentos(reloj);
        String clave = "login:ana@correo.test";

        for (int i = 0; i < LimitadorIntentos.INTENTOS_POR_ETAPA_LOGIN - 1; i++) {
            int restantes = limitador.registrarFalloLogin(clave);
            assertEquals(LimitadorIntentos.INTENTOS_POR_ETAPA_LOGIN - i - 1, restantes);
        }
        var primerBloqueo = assertThrows(DemasiadosIntentosException.class,
                () -> limitador.registrarFalloLogin(clave));
        assertEquals(180L, primerBloqueo.getSegundosRestantes());
        assertEquals(5, primerBloqueo.getIntentosRestantes());
        assertThrows(DemasiadosIntentosException.class, () -> limitador.comprobarLoginPermitido(clave));

        reloj.ahora = reloj.ahora.plus(LimitadorIntentos.BLOQUEO_LOGIN_INICIAL);
        for (int i = 0; i < LimitadorIntentos.INTENTOS_POR_ETAPA_LOGIN - 1; i++) {
            assertDoesNotThrow(() -> limitador.registrarFalloLogin(clave));
        }
        var segundoBloqueo = assertThrows(DemasiadosIntentosException.class,
                () -> limitador.registrarFalloLogin(clave));
        assertEquals(600L, segundoBloqueo.getSegundosRestantes());
        assertEquals(5, segundoBloqueo.getIntentosRestantes());

        reloj.ahora = reloj.ahora.plus(LimitadorIntentos.BLOQUEO_LOGIN_SEGUNDO);
        for (int i = 0; i < LimitadorIntentos.INTENTOS_POR_ETAPA_LOGIN - 1; i++) {
            assertDoesNotThrow(() -> limitador.registrarFalloLogin(clave));
        }
        var bloqueoPermanente = assertThrows(DemasiadosIntentosException.class,
                () -> limitador.registrarFalloLogin(clave));
        assertEquals("CUENTA_BLOQUEADA", bloqueoPermanente.getCodigo());
        assertEquals(0, bloqueoPermanente.getIntentosRestantes());
        assertThrows(DemasiadosIntentosException.class, () -> limitador.comprobarLoginPermitido(clave));

        limitador.desbloquearLogin(clave);
        assertDoesNotThrow(() -> limitador.comprobarLoginPermitido(clave));
    }

}
