package co.edu.unicesar.reda.model;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AgrupadorPuntosTest {

    /** Un reporte de prueba: orden cronológico y coordenadas. */
    record R(int orden, double lat, double lon) {
    }

    private final AgrupadorPuntos<R> agrupador = new AgrupadorPuntos<>(80, R::lat, R::lon);
    private final Comparator<R> porOrden = Comparator.comparingInt(R::orden);

    @Test
    void reportesAMenosDelRadioFormanUnSoloPunto() {
        // ~0.0003° de latitud ≈ 33 m
        var grupos = agrupador.agrupar(List.of(new R(1, 10.4771, -73.2448), new R(2, 10.4774, -73.2448),
                new R(3, 10.4772, -73.2450)), porOrden);
        assertEquals(1, grupos.size());
        assertEquals(3, grupos.get(0).tamano());
    }

    @Test
    void reportesLejanosQuedanEnPuntosDistintos() {
        // ~0.002° ≈ 220 m
        var grupos = agrupador.agrupar(List.of(new R(1, 10.4771, -73.2448), new R(2, 10.4791, -73.2448)), porOrden);
        assertEquals(2, grupos.size());
    }

    @Test
    void elCentroEsElPromedioDeLasCoordenadas() {
        var g = agrupador.agrupar(List.of(new R(1, 10.4770, -73.2440), new R(2, 10.4774, -73.2444)), porOrden).get(0);
        assertEquals(10.4772, g.latitudCentro(), 1e-9);
        assertEquals(-73.2442, g.longitudCentro(), 1e-9);
    }

    @Test
    void elResultadoNoDependeDelOrdenDeLaListaDeEntrada() {
        var a = new R(1, 10.4771, -73.2448);
        var b = new R(2, 10.4774, -73.2448);
        var c = new R(3, 10.4900, -73.2600);
        assertEquals(agrupador.agrupar(List.of(a, b, c), porOrden).size(),
                agrupador.agrupar(List.of(c, b, a), porOrden).size());
    }

    @Test
    void laDistanciaDeHaversineEsCorrecta() {
        // un grado de latitud mide ~111,2 km sobre la esfera de radio 6 371 km
        assertEquals(111_195, Distancia.metros(10, -73, 11, -73), 50);
        assertEquals(0, Distancia.metros(10.4771, -73.2448, 10.4771, -73.2448), 1e-9);
    }

    @Test
    void laFrecuenciaSeClasificaPorNumeroDeReportes() {
        assertEquals(Frecuencia.UNICA, Frecuencia.de(1));
        assertEquals(Frecuencia.MEDIA, Frecuencia.de(2));
        assertEquals(Frecuencia.ALTA, Frecuencia.de(3));
        assertEquals(Frecuencia.CRITICA, Frecuencia.de(5));
    }

    @Test
    void elRadioDebeSerPositivo() {
        assertThrows(IllegalArgumentException.class, () -> new AgrupadorPuntos<R>(0, R::lat, R::lon));
    }
}
