package co.edu.unicesar.reda.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

/** Vínculo entre dos reportes: SIMILAR o ANTECEDENTE (incidente -> condición previa en el mismo punto). */
@Entity
@Table(name = "reportes_relacionados")
public class ReporteRelacionado {

    public enum Tipo { SIMILAR, ANTECEDENTE }

    public enum Origen { AUTOMATICO, MANUAL }

    @Embeddable
    public static class Clave implements Serializable {

        @Column(name = "id_reporte")
        private Integer idReporte;

        @Column(name = "id_reporte_relacionado")
        private Integer idReporteRelacionado;

        protected Clave() {
        }

        public Clave(Integer idReporte, Integer idReporteRelacionado) {
            this.idReporte = idReporte;
            this.idReporteRelacionado = idReporteRelacionado;
        }

        public Integer getIdReporte() { return idReporte; }
        public Integer getIdReporteRelacionado() { return idReporteRelacionado; }

        @Override
        public boolean equals(Object o) {
            return o instanceof Clave c && Objects.equals(idReporte, c.idReporte)
                    && Objects.equals(idReporteRelacionado, c.idReporteRelacionado);
        }

        @Override
        public int hashCode() {
            return Objects.hash(idReporte, idReporteRelacionado);
        }
    }

    @EmbeddedId
    private Clave clave;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_relacion", nullable = false, length = 12)
    private Tipo tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen", nullable = false, length = 10)
    private Origen origen;

    @Column(name = "fecha_vinculo", nullable = false)
    private LocalDate fechaVinculo;

    protected ReporteRelacionado() {
    }

    public static ReporteRelacionado antecedenteAutomatico(Integer idIncidente, Integer idCondicion) {
        ReporteRelacionado r = new ReporteRelacionado();
        r.clave = new Clave(idIncidente, idCondicion);
        r.tipo = Tipo.ANTECEDENTE;
        r.origen = Origen.AUTOMATICO;
        r.fechaVinculo = LocalDate.now(ZoneId.of("America/Bogota"));
        return r;
    }

    public Clave getClave() { return clave; }
    public Tipo getTipo() { return tipo; }
    public Origen getOrigen() { return origen; }
}
