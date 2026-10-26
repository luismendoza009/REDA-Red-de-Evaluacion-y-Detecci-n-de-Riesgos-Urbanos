package co.edu.unicesar.reda.model;

import co.edu.unicesar.reda.exception.ReglaNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Reglas del flujo "varios abogados se postulan y el ciudadano elige", sin Spring ni base de datos. */
class CasoTest {

    private final EstadoCaso remitido = EstadoCaso.de(3, EstadoCaso.Nombre.REMITIDO);
    private final EstadoCaso aceptado = EstadoCaso.de(4, EstadoCaso.Nombre.ACEPTADO);
    private final EstadoCaso cerrado = EstadoCaso.de(6, EstadoCaso.Nombre.CERRADO);

    private final Usuario ciudadana = usuario(10, NombreRol.CIUDADANO);
    private final Usuario vecino = usuario(11, NombreRol.CIUDADANO);
    private final Usuario operador = usuario(2, NombreRol.OPERADOR);
    private final Abogado abogadaA = abogado(1, usuario(20, NombreRol.ABOGADO), true);
    private final Abogado abogadoB = abogado(2, usuario(21, NombreRol.ABOGADO), true);

    private Incidente incidente;

    private static Usuario usuario(int id, NombreRol rol) {
        Usuario u = mock(Usuario.class);
        when(u.getId()).thenReturn(id);
        when(u.tieneRol(rol)).thenReturn(true);
        when(u.esPersonalDeRevision()).thenReturn(rol == NombreRol.OPERADOR || rol == NombreRol.ADMINISTRADOR);
        return u;
    }

    private static Abogado abogado(int id, Usuario u, boolean activo) {
        Abogado a = mock(Abogado.class);
        when(a.getId()).thenReturn(id);
        when(a.isActivo()).thenReturn(activo);
        when(a.getUsuario()).thenReturn(u);
        when(a.esUsuario(u)).thenReturn(true);
        return a;
    }

    @BeforeEach
    void incidenteValidado() {
        Reporte r = Reporte.nuevo(TipoReporte.INCIDENTE, ciudadana, null, null, Estado.de(1, NombreEstado.PENDIENTE),
                "Peatón cayó en la alcantarilla sin tapa", BigDecimal.ONE, BigDecimal.ONE, (short) 5);
        incidente = r.registrarIncidente(TipoDano.LESION, "Fractura de tibia", OffsetDateTime.now().minusHours(2), true, null);
        r.cambiarEstado(Estado.de(2, NombreEstado.EN_REVISION), operador, null);
        r.cambiarEstado(Estado.de(3, NombreEstado.VALIDADO), operador, "Verificado");
    }

    private Caso casoConDosPostulaciones() {
        Caso c = incidente.solicitarAbogado(ciudadana, operador, remitido, true);
        ReflectionTestUtils.setField(c.postular(abogadaA, "Llevo casos de reparación directa hace años"), "id", 100);
        ReflectionTestUtils.setField(c.postular(abogadoB, "Puedo revisar su caso esta misma semana"), "id", 200);
        return c;
    }

    @Test
    void soloElAutorPuedePedirAbogadoYConAutorizacion() {
        assertEquals("NO_ES_AUTOR", assertThrows(ReglaNegocioException.class,
                () -> incidente.solicitarAbogado(vecino, operador, remitido, true)).getCodigo());
        assertEquals("SIN_AUTORIZACION", assertThrows(ReglaNegocioException.class,
                () -> incidente.solicitarAbogado(ciudadana, operador, remitido, false)).getCodigo());
    }

    @Test
    void unIncidenteSinValidarNoPuedeBuscarAbogado() {
        Reporte r = Reporte.nuevo(TipoReporte.INCIDENTE, ciudadana, null, null, Estado.de(1, NombreEstado.PENDIENTE),
                "Hueco que dañó la llanta del carro", BigDecimal.ONE, BigDecimal.ONE, null);
        Incidente sinValidar = r.registrarIncidente(TipoDano.DANO_MATERIAL, "Llanta destruida", OffsetDateTime.now().minusHours(1), false, null);
        assertEquals("INCIDENTE_NO_VALIDADO", assertThrows(ReglaNegocioException.class,
                () -> sinValidar.solicitarAbogado(ciudadana, operador, remitido, true)).getCodigo());
    }

    @Test
    void unIncidenteTieneUnSoloCaso() {
        incidente.solicitarAbogado(ciudadana, operador, remitido, true);
        assertEquals("CASO_EXISTENTE", assertThrows(ReglaNegocioException.class,
                () -> incidente.solicitarAbogado(ciudadana, operador, remitido, true)).getCodigo());
    }

    @Test
    void variosAbogadosSePostulanPeroNingunoDosVeces() {
        Caso c = casoConDosPostulaciones();
        assertEquals(2, c.getPostulaciones().size());
        assertEquals("YA_POSTULADO", assertThrows(ReglaNegocioException.class,
                () -> c.postular(abogadaA, "Insisto en que quiero el caso")).getCodigo());
    }

    @Test
    void unAbogadoSinVerificarNoSePostula() {
        Caso c = incidente.solicitarAbogado(ciudadana, operador, remitido, true);
        Abogado nuevo = abogado(3, usuario(22, NombreRol.ABOGADO), false);
        assertEquals("ABOGADO_NO_VERIFICADO", assertThrows(ReglaNegocioException.class,
                () -> c.postular(nuevo, "Quiero tomar este caso de inmediato")).getCodigo());
    }

    @Test
    void elCiudadanoEligeYLasDemasPostulacionesQuedanNoElegidas() {
        Caso c = casoConDosPostulaciones();

        Remision r = c.elegir(200, ciudadana, aceptado);

        assertEquals(EstadoCaso.Nombre.ACEPTADO, c.getEstado().tipo());
        assertSame(abogadoB, r.getAbogado());
        assertEquals(TarifaRemision.CON_LESION, r.getValorCobrado(), "con lesión se cobra la tarifa mayor");
        assertEquals(Postulacion.Estado.NO_ELEGIDA, c.getPostulaciones().get(0).getEstado());
        assertEquals(Postulacion.Estado.ELEGIDA, c.getPostulaciones().get(1).getEstado());
        assertTrue(c.esAbogadoAsignado(abogadoB.getUsuario()));
        assertFalse(c.esAbogadoAsignado(abogadaA.getUsuario()));
    }

    @Test
    void soloElDuenoEligeYSoloUnaVez() {
        Caso c = casoConDosPostulaciones();
        assertEquals("NO_ES_AUTOR", assertThrows(ReglaNegocioException.class, () -> c.elegir(100, vecino, aceptado)).getCodigo());
        c.elegir(100, ciudadana, aceptado);
        assertEquals("CASO_NO_DISPONIBLE", assertThrows(ReglaNegocioException.class, () -> c.elegir(200, ciudadana, aceptado)).getCodigo());
    }

    @Test
    void noSePuedeElegirUnaPostulacionRetirada() {
        Caso c = casoConDosPostulaciones();
        c.retirarPostulacion(abogadaA);
        assertEquals("POSTULACION_INVALIDA", assertThrows(ReglaNegocioException.class, () -> c.elegir(100, ciudadana, aceptado)).getCodigo());
    }

    @Test
    void soloElAbogadoElegidoCierraElCaso() {
        Caso c = casoConDosPostulaciones();
        c.elegir(100, ciudadana, aceptado);
        assertEquals("NO_ES_ABOGADO_DEL_CASO", assertThrows(ReglaNegocioException.class,
                () -> c.cerrar(abogadoB.getUsuario(), cerrado, "Cierro el caso porque sí")).getCodigo());
        c.cerrar(abogadaA.getUsuario(), cerrado, "Conciliación con la entidad");
        assertEquals(EstadoCaso.Nombre.CERRADO, c.getEstado().tipo());
        assertNotNull(c.getFechaCierre());
    }

    @Test
    void elAbogadoElegidoPuedeVerElReporteYLosDemasNo() {
        Caso c = casoConDosPostulaciones();
        Reporte reporte = incidente.getReporte();
        assertFalse(reporte.puedeSerVistoPor(abogadaA.getUsuario()), "antes de elegir, ningún abogado ve el detalle");
        c.elegir(100, ciudadana, aceptado);
        assertTrue(reporte.puedeSerVistoPor(abogadaA.getUsuario()));
        assertFalse(reporte.puedeSerVistoPor(abogadoB.getUsuario()));
    }

    @Test
    void elTokenDeRecuperacionSirveUnaSolaVezYVence() {
        Usuario u = usuario(30, NombreRol.CIUDADANO);
        TokenRecuperacion t = TokenRecuperacion.para(u, "a".repeat(64));
        OffsetDateTime ahora = OffsetDateTime.now();
        assertTrue(t.sirveEn(ahora));
        assertFalse(t.sirveEn(ahora.plus(TokenRecuperacion.VIGENCIA).plusSeconds(1)), "vence a los 30 minutos");
        t.usar(ahora);
        assertFalse(t.sirveEn(ahora), "un token usado no vuelve a servir");
        assertThrows(IllegalStateException.class, () -> t.usar(ahora));
    }
}
