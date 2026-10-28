package co.edu.unicesar.reda.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Agrupa reportes cercanos en "puntos" del mapa: un mismo hueco reportado varias veces es UN punto
 * con varios reportes, no varios marcadores encimados. Así aparecen los puntos de alta frecuencia.
 *
 * Algoritmo (agrupamiento voraz por distancia, sencillo y predecible):
 *   1. se recorren los elementos del más antiguo al más reciente;
 *   2. cada uno se une al grupo cuyo centro esté más cerca, si está a menos del radio;
 *   3. si ninguno está a menos del radio, abre un grupo nuevo;
 *   4. el centro del grupo se recalcula como el promedio de sus coordenadas.
 * Es genérico (sirve para cualquier tipo T) y no depende de Spring ni de la base: se prueba solo.
 */
public final class AgrupadorPuntos<T> {

    private final double radioMetros;
    private final Function<T, Double> latitud;
    private final Function<T, Double> longitud;

    public AgrupadorPuntos(double radioMetros, Function<T, Double> latitud, Function<T, Double> longitud) {
        if (radioMetros <= 0) {
            throw new IllegalArgumentException("El radio debe ser positivo");
        }
        this.radioMetros = radioMetros;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public List<Grupo<T>> agrupar(List<T> elementos, Comparator<T> ordenCronologico) {
        List<Grupo<T>> grupos = new ArrayList<>();
        elementos.stream().sorted(ordenCronologico).forEach(e -> {
            double lat = latitud.apply(e);
            double lon = longitud.apply(e);
            grupos.stream()
                    .filter(g -> g.distanciaA(lat, lon) <= radioMetros)
                    .min(Comparator.comparingDouble(g -> g.distanciaA(lat, lon)))
                    .ifPresentOrElse(g -> g.agregar(e, lat, lon), () -> grupos.add(new Grupo<>(e, lat, lon)));
        });
        return grupos;
    }

    /** Un punto del mapa: su centro y los elementos que contiene. */
    public static final class Grupo<T> {
        private final List<T> elementos = new ArrayList<>();
        private double sumaLat;
        private double sumaLon;

        private Grupo(T primero, double lat, double lon) {
            agregar(primero, lat, lon);
        }

        private void agregar(T e, double lat, double lon) {
            elementos.add(e);
            sumaLat += lat;
            sumaLon += lon;
        }

        double distanciaA(double lat, double lon) {
            return Distancia.metros(latitudCentro(), longitudCentro(), lat, lon);
        }

        public double latitudCentro() { return sumaLat / elementos.size(); }
        public double longitudCentro() { return sumaLon / elementos.size(); }
        public List<T> elementos() { return List.copyOf(elementos); }
        public int tamano() { return elementos.size(); }
    }
}
