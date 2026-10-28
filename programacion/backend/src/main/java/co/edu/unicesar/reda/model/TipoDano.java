package co.edu.unicesar.reda.model;

public enum TipoDano {
    LESION, DANO_MATERIAL, AMBOS;

    /** Hubo una persona lesionada (solo entonces tiene sentido registrar atención médica). */
    public boolean incluyeLesion() {
        return this == LESION || this == AMBOS;
    }
}
