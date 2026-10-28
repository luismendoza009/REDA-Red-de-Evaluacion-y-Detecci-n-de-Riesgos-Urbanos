package co.edu.unicesar.reda.model;

/**
 * Qué tanto se repite una falla en un mismo punto. Los cortes (2, 3, 5) son preliminares,
 * igual que los niveles de los índices: se calibrarán con datos reales.
 */
public enum Frecuencia {
    UNICA, MEDIA, ALTA, CRITICA;

    public static Frecuencia de(int reportesEnElPunto) {
        if (reportesEnElPunto >= 5) {
            return CRITICA;
        }
        if (reportesEnElPunto >= 3) {
            return ALTA;
        }
        return reportesEnElPunto == 2 ? MEDIA : UNICA;
    }
}
