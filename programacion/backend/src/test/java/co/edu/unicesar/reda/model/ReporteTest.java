package co.edu.unicesar.reda.model;

import co.edu.unicesar.reda.exception.ReglaNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Reglas del dominio probadas sin Spring ni base de datos: el modelo es POO pura. */
class ReporteTest {

    private final Estado pendiente = Estado.de(1, NombreEstado.PENDIENTE);
    private final Estado enRevision = Estado.de(2, NombreEstado.EN_REVISION);
    private final Estado validado = Estado.de(3, NombreEstado.VALIDADO);
    private final Estado rechazado = Estado.de(6, NombreEstado.RECHAZADO);

    private final Usuario ciudadano = Usuario.registrar("Ana Pérez", "Ana@Correo.TEST", "$2a$10$hash",
            Rol.de(1, NombreRol.CIUDADANO));
    private final Usuario operador = Usuario.registrar("Luis Daza", "luis@correo.test", "$2a$10$hash",
            Rol.de(2, NombreRol.OPERADOR));

    private Reporte reporte(TipoReporte tipo) {
        return Reporte.nuevo(tipo, ciudadano, null, null, pendiente, "  Alcantarilla sin tapa en la esquina  ",
                new BigDecimal("10.477104"), new BigDecimal("-73.244831"), (short) 5);
    }

    @Test
    void unReporteNuevoNaceEnPendienteYSinHistorial() {
        Reporte r = reporte(TipoReporte.CONDICION);
        assertEquals(NombreEstado.PENDIENTE, r.getEstado().tipo());
        assertTrue(r.getHistorial().isEmpty());
        assertEquals("Alcantarilla sin tapa en la esquina", r.getDescripcion(), "la descripción se recorta");
    }

    @Test
    void noSePuedeCrearUnReporteEnOtroEstadoQuePendiente() {
        assertThrows(IllegalArgumentException.class, () -> Reporte.nuevo(TipoReporte.CONDICION, ciudadano, null,
                null, validado, "descripción válida", BigDecimal.ONE, BigDecimal.ONE, null));
    }

    @Test
    void elCorreoSeNormalizaAMinusculas() {
        assertEquals("ana@correo.test", ciudadano.getCorreo());
    }

    @Nested
    class CambioDeEstado {

        private Reporte r;

        @BeforeEach
        void crear() {
            r = reporte(TipoReporte.CONDICION);
        }

        @Test
        void cambiaElEstadoYDejaConstanciaEnElHistorial() {
            HistorialEstado h = r.cambiarEstado(enRevision, operador, " Se revisa la foto ");

            assertEquals(NombreEstado.EN_REVISION, r.getEstado().tipo());
            assertEquals(1, r.getHistorial().size());
            assertSame(h, r.getHistorial().get(0));
            assertEquals(NombreEstado.PENDIENTE, h.getEstadoAnterior().tipo());
            assertEquals("Se revisa la foto", h.getObservacion());
            assertSame(operador, h.getResponsable());
        }

        @Test
        void rechazaUnaTransicionNoPermitidaSinTocarElEstado() {
            var e = assertThrows(ReglaNegocioException.class, () -> r.cambiarEstado(validado, operador, null));
            assertEquals("TRANSICION_INVALIDA", e.getCodigo());
            assertEquals(NombreEstado.PENDIENTE, r.getEstado().tipo(), "el estado no debe cambiar si la regla falla");
            assertTrue(r.getHistorial().isEmpty());
        }

        @Test
        void rechazarExigeObservacion() {
            var e = assertThrows(ReglaNegocioException.class, () -> r.cambiarEstado(rechazado, operador, "   "));
            assertEquals("OBSERVACION_REQUERIDA", e.getCodigo());
        }

        @Test
        void unCiudadanoNoPuedeCambiarEstados() {
            var e = assertThrows(ReglaNegocioException.class, () -> r.cambiarEstado(enRevision, ciudadano, null));
            assertEquals("SIN_PERMISO_ESTADO", e.getCodigo());
        }

        @Test
        void elHistorialNoSePuedeModificarDesdeFuera() {
            assertThrows(UnsupportedOperationException.class, () -> r.getHistorial().clear());
        }
    }

    @Nested
    class Incidentes {

        private final OffsetDateTime haceUnaHora = OffsetDateTime.now().minusHours(1);

        @Test
        void unIncidenteRegistraSusDatosDeDano() {
            Reporte r = reporte(TipoReporte.INCIDENTE);
            Incidente i = r.registrarIncidente(TipoDano.LESION, "Fractura de muñeca", haceUnaHora, true, null);
            assertSame(i, r.getIncidente());
            assertTrue(i.isHuboAtencionMedica());
        }

        @Test
        void unaCondicionNoPuedeTenerDatosDeDano() {
            Reporte r = reporte(TipoReporte.CONDICION);
            var e = assertThrows(ReglaNegocioException.class,
                    () -> r.registrarIncidente(TipoDano.LESION, "Fractura de muñeca", haceUnaHora, true, null));
            assertEquals("NO_ES_INCIDENTE", e.getCodigo());
        }

        @Test
        void noHayAtencionMedicaSinLesion() {
            Reporte r = reporte(TipoReporte.INCIDENTE);
            var e = assertThrows(ReglaNegocioException.class,
                    () -> r.registrarIncidente(TipoDano.DANO_MATERIAL, "Llanta destruida", haceUnaHora, true, null));
            assertEquals("ATENCION_SIN_LESION", e.getCodigo());
        }

        @Test
        void elHechoNoPuedeSerPosteriorAlReporte() {
            Reporte r = reporte(TipoReporte.INCIDENTE);
            var e = assertThrows(ReglaNegocioException.class, () -> r.registrarIncidente(TipoDano.LESION,
                    "Fractura de muñeca", OffsetDateTime.now().plusDays(1), false, null));
            assertEquals("FECHA_HECHO_FUTURA", e.getCodigo());
        }

        @Test
        void soloSeRegistraUnIncidentePorReporte() {
            Reporte r = reporte(TipoReporte.INCIDENTE);
            r.registrarIncidente(TipoDano.LESION, "Fractura de muñeca", haceUnaHora, false, null);
            var e = assertThrows(ReglaNegocioException.class,
                    () -> r.registrarIncidente(TipoDano.LESION, "Otra lesión", haceUnaHora, false, null));
            assertEquals("INCIDENTE_DUPLICADO", e.getCodigo());
        }
    }
}
