package co.edu.unicesar.reda.model;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 * Estados del ciclo de atención de un reporte y sus transiciones permitidas.
 *
 *   Pendiente -> En revisión -> Validado -> En proceso -> Solucionado
 *        \            \
 *         +-----------+--> Rechazado -> En revisión (se puede reabrir)
 *
 * GRASP Experto en información: quien conoce el ciclo de vida es el propio estado, así que
 * la regla "¿puedo pasar de A a B?" vive aquí y no repartida en servicios o controladores.
 */
public enum NombreEstado {
    PENDIENTE("Pendiente"),
    EN_REVISION("En revisión"),
    VALIDADO("Validado"),
    EN_PROCESO("En proceso"),
    SOLUCIONADO("Solucionado"),
    RECHAZADO("Rechazado");

    private final String etiqueta;

    NombreEstado(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** Nombre tal como está guardado en la tabla estados. */
    public String etiqueta() {
        return etiqueta;
    }

    public Set<NombreEstado> siguientesPermitidos() {
        return switch (this) {
            case PENDIENTE -> EnumSet.of(EN_REVISION, RECHAZADO);
            case EN_REVISION -> EnumSet.of(VALIDADO, RECHAZADO);
            case VALIDADO -> EnumSet.of(EN_PROCESO);
            case EN_PROCESO -> EnumSet.of(SOLUCIONADO);
            case RECHAZADO -> EnumSet.of(EN_REVISION);
            case SOLUCIONADO -> EnumSet.noneOf(NombreEstado.class);
        };
    }

    public boolean puedePasarA(NombreEstado destino) {
        return siguientesPermitidos().contains(destino);
    }

    public static NombreEstado desdeEtiqueta(String etiqueta) {
        return Arrays.stream(values())
                .filter(e -> e.etiqueta.equals(etiqueta))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Estado desconocido en la base: " + etiqueta));
    }
}
