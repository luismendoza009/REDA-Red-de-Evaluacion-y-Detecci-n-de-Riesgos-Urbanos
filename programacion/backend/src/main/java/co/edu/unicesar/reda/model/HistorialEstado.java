package co.edu.unicesar.reda.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

/** Registro inmutable de un cambio de estado (bitácora). Solo lo crea {@link Reporte#cambiarEstado}. */
@Entity
@Table(name = "historial_estados")
public class HistorialEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_reporte")
    private Reporte reporte;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_estado_anterior")
    private Estado estadoAnterior;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_estado_nuevo")
    private Estado estadoNuevo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario")
    private Usuario responsable;

    @Column(name = "fecha_cambio", nullable = false)
    private OffsetDateTime fechaCambio;

    @Column(name = "observacion", columnDefinition = "text")
    private String observacion;

    protected HistorialEstado() {
    }

    static HistorialEstado de(Reporte reporte, Estado anterior, Estado nuevo, Usuario responsable, String observacion) {
        HistorialEstado h = new HistorialEstado();
        h.reporte = reporte;
        h.estadoAnterior = anterior;
        h.estadoNuevo = nuevo;
        h.responsable = responsable;
        h.fechaCambio = OffsetDateTime.now();
        h.observacion = (observacion == null || observacion.isBlank()) ? null : observacion.trim();
        return h;
    }

    public Integer getId() { return id; }
    public Estado getEstadoAnterior() { return estadoAnterior; }
    public Estado getEstadoNuevo() { return estadoNuevo; }
    public Usuario getResponsable() { return responsable; }
    public OffsetDateTime getFechaCambio() { return fechaCambio; }
    public String getObservacion() { return observacion; }
}
