package co.edu.unicesar.reda.model;

/**
 * Distancia entre dos coordenadas sobre la superficie terrestre (fórmula de Haversine).
 * Es la misma fórmula que usa la base de datos (consulta C05 y buscador de antecedentes).
 */
public final class Distancia {

    private static final double RADIO_TIERRA_METROS = 6_371_000;

    private Distancia() {
    }

    public static double metros(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.pow(Math.sin(dLon / 2), 2);
        return 2 * RADIO_TIERRA_METROS * Math.asin(Math.sqrt(a));
    }
}
