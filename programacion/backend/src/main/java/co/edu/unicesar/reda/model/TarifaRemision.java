package co.edu.unicesar.reda.model;

import java.math.BigDecimal;

/**
 * Cuánto se le cobra al abogado cuando un ciudadano lo elige (cuota fija por caso, NO cuota litis).
 * Valores SIMULADOS (decisión D13): sin estudio de mercado. Se aísla en una clase para cambiarlos
 * en un solo lugar (y, en el futuro, leerlos de la base).
 */
public final class TarifaRemision {

    public static final BigDecimal CON_LESION = new BigDecimal("200000.00");
    public static final BigDecimal SOLO_MATERIAL = new BigDecimal("120000.00");

    private TarifaRemision() {
    }

    public static BigDecimal para(TipoDano tipoDano) {
        return tipoDano.incluyeLesion() ? CON_LESION : SOLO_MATERIAL;
    }
}
