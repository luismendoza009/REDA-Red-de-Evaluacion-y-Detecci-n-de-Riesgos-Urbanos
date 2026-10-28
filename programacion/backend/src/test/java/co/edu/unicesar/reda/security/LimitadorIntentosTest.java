package co.edu.unicesar.reda.security;

import co.edu.unicesar.reda.exception.DemasiadosIntentosException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
