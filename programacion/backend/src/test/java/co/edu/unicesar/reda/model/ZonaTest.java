package co.edu.unicesar.reda.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Sugerencia del barrio según el punto marcado, con puntos de referencia reales de Valledupar (OSM). */
class ZonaTest {

    private final Zona centro = new Zona(1, "Centro", new BigDecimal("10.47765"), new BigDecimal("-73.24455"));
    private final Zona laNevada = new Zona(15, "La Nevada", new BigDecimal("10.47657"), new BigDecimal("-73.28280"));
    private final Zona upc = new Zona(19, "Universidad Popular del Cesar", new BigDecimal("10.44950"), new BigDecimal("-73.26240"));
    private final Zona sinUbicar = new Zona(31, "Barrio nuevo", null, null);
    private final List<Zona> zonas = List.of(centro, laNevada, upc, sinUbicar);

    @Test
    void unPuntoEnLaUpcSugiereLaUpcYNoLaNevada() {
        // el caso que reportó el usuario: un punto dentro del campus quedaba marcado como "La Nevada"
        assertSame(upc, Zona.masCercana(zonas, 10.4497, -73.2620, 600).orElseThrow());
    }

    @Test
    void sugiereElBarrioMasCercano() {
        assertSame(centro, Zona.masCercana(zonas, 10.4770, -73.2450, 600).orElseThrow());
        assertSame(laNevada, Zona.masCercana(zonas, 10.4760, -73.2820, 600).orElseThrow());
    }

    @Test
    void siNingunBarrioQuedaCercaNoSugiereNada() {
        // punto a unos 2 km de todos los barrios ubicados: es mejor que el ciudadano elija
        assertTrue(Zona.masCercana(zonas, 10.4950, -73.2200, 600).isEmpty());
    }

    @Test
    void unaZonaSinUbicacionNoParticipa() {
        assertTrue(sinUbicar.distanciaA(10.47, -73.25).isEmpty());
        assertTrue(Zona.masCercana(List.of(sinUbicar), 10.47, -73.25, 10_000).isEmpty());
    }

    @Test
    void laDistanciaEsHaversine() {
        assertEquals(0, centro.distanciaA(10.47765, -73.24455).getAsDouble(), 1e-6);
        // del Centro a La Nevada hay unos 4,2 km
        assertEquals(4185, laNevada.distanciaA(10.47765, -73.24455).getAsDouble(), 25);
    }
}
